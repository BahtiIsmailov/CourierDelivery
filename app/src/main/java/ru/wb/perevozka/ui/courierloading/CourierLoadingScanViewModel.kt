package ru.wb.perevozka.ui.courierloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateHandler
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingInteractor
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingProcessData
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingProgressData
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingScanBoxData
import ru.wb.perevozka.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.perevozka.ui.scanner.domain.ScannerAction
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.time.DateTimeFormatter
import java.util.concurrent.TimeUnit

class CourierLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierScannerLoadingResourceProvider,
    private val interactor: CourierLoadingInteractor,
    private val courierOrderTimerInteractor: CourierOrderTimerInteractor,
) : TimerStateHandler, NetworkViewModel(compositeDisposable) {

    private val _orderTimer = MutableLiveData<CourierLoadingScanTimerState>()
    val orderTimer: LiveData<CourierLoadingScanTimerState>
        get() = _orderTimer

    private val _navigationEvent =
        SingleLiveEvent<CourierLoadingScanNavAction>()
    val navigationEvent: LiveData<CourierLoadingScanNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _beepEvent =
        SingleLiveEvent<CourierLoadingScanBeepState>()
    val beepEvent: LiveData<CourierLoadingScanBeepState>
        get() = _beepEvent

    private val _progressEvent =
        SingleLiveEvent<CourierLoadingScanProgress>()
    val progressEvent: LiveData<CourierLoadingScanProgress>
        get() = _progressEvent

    private val _boxStateUI =
        MutableLiveData<CourierLoadingScanBoxState>()
    val boxStateUI: LiveData<CourierLoadingScanBoxState>
        get() = _boxStateUI

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        observeNetworkState()
        observeInitScanProcess()
        observeScanProcess()
        observeTimer()
    }

    private fun observeTimer() {
        addSubscription(courierOrderTimerInteractor.timer
            .subscribe({ onHandleSignUpState(it) }) { onHandleSignUpError() })
    }

    private fun onHandleSignUpState(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun onHandleSignUpError() {}

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun observeInitScanProcess() {
        addSubscription(interactor.scannedBoxes()
            .map { list ->
                if (list.isEmpty()) CourierLoadingScanBoxState.Empty
                else {
                    val lastBox = list.last()
                    CourierLoadingScanBoxState.BoxInit(
                        lastBox.qrcode,
                        lastBox.address,
                        list.size.toString(),
                    )
                }
            }.subscribe(
                { _boxStateUI.value = it },
                { LogUtils { logDebugApp(it.toString()) } }
            )
        )
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeScanProcess()
            .doOnError { observeScanProcessError(it) }
            .retryWhen { errorObservable -> errorObservable.delay(1, TimeUnit.SECONDS) }
            .subscribe(
                { observeScanProcessComplete(it) },
                { observeScanProcessError(it) }
            )
        )
    }

    private fun observeScanProgress() {
        addSubscription(interactor.scanLoaderProgress()
            .subscribe {
                _progressEvent.value = when (it) {
                    CourierLoadingProgressData.Complete -> {
                        interactor.scannerAction(ScannerAction.LoaderComplete)
                        CourierLoadingScanProgress.LoaderComplete
                    }
                    CourierLoadingProgressData.Progress -> {
                        interactor.scannerAction(ScannerAction.LoaderProgress)
                        CourierLoadingScanProgress.LoaderProgress
                    }
                }
            })
    }

    private fun observeScanProcessComplete(scanProcess: CourierLoadingProcessData) {
        val scanBoxData = scanProcess.scanBoxData
        val accepted = scanProcess.count.toString()
        when (scanBoxData) {
            is CourierLoadingScanBoxData.BoxAdded -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierLoadingScanBoxState.BoxAdded(qrCode, address, accepted)
                }
                _beepEvent.value = CourierLoadingScanBeepState.BoxAdded
            }

            is CourierLoadingScanBoxData.UnknownBox -> {
                _navigationEvent.value = CourierLoadingScanNavAction.NavigateToUnknownBox
                _boxStateUI.value =
                    with(scanBoxData) {
                        CourierLoadingScanBoxState.UnknownBox(
                            "?",
                            "Коробка с другого маршрута",
                            accepted
                        )
                    }
                _beepEvent.value = CourierLoadingScanBeepState.UnknownBox
            }
            CourierLoadingScanBoxData.Empty -> _boxStateUI.value = CourierLoadingScanBoxState.Empty
        }
    }

    fun onListClicked() {
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToBoxes
    }

    fun onCompleteClicked() {
        bottomProgressEvent.value = true
        addSubscription(
            interactor.switchScreen().subscribe(
                {
                    _navigationEvent.value =
                        CourierLoadingScanNavAction.NavigateToFlightDeliveries
                    bottomProgressEvent.value = false
                },
                {
                    bottomProgressEvent.value = false
                    switchScreenError(it)
                })
        )
    }

    private fun switchScreenError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getSwitchDialogButton()
        }
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton()
        )
    }

    private fun observeScanProcessError(throwable: Throwable) {
        val error = if (throwable is CompositeException) {
            throwable.exceptions[0]
        } else throwable
        scanProcessError(error)
    }

    private fun scanProcessError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        interactor.scannerAction(ScannerAction.Stop)
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton()
        )
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerAction.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerAction.Start)
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

    override fun onTimerState(duration: Int) {
        updateTimer(duration)
    }

    private fun updateTimer(duration: Int) {
        _orderTimer.value =
            CourierLoadingScanTimerState.Timer(
                DateTimeFormatter.getAnalogTime(duration),
                DateTimeFormatter.getDigitTime(duration)
            )
    }

    override fun onTimeIsOverState() {
        _orderTimer.value = CourierLoadingScanTimerState.TimeIsOut
    }

}