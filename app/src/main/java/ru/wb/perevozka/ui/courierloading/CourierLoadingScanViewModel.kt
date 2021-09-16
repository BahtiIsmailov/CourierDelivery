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
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.ui.scanner.domain.ScannerAction
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.time.DateTimeFormatter
import java.util.concurrent.TimeUnit

class CourierLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierLoadingResourceProvider,
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

    private val _bottomEvent =
        MutableLiveData<CourierLoadingScanBottomState>()
    val bottomProgressEvent: LiveData<CourierLoadingScanBottomState>
        get() = _bottomEvent

    //val  = MutableLiveData<Boolean>()

    init {
        observeNetworkState()
        observeInitScanProcess()
        observeScanProcess()
        observeScanProgress()
    }

    private fun observeTimer() {
        addSubscription(
            courierOrderTimerInteractor.timer
                .subscribe({ observeTimerComplete(it) }, { observeTimerError() })
        )
    }

    private fun observeTimerComplete(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun observeTimerError() {}

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    fun confirmLoadingClick() {
        onStopScanner()
        // TODO: 14.09.2021 progress state
        addSubscription(
            interactor.confirmLoading()
                .subscribe(
                    {
                        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToIntransit
                    },
                    { onStartScanner() }
                )
        )
    }

    private fun observeInitScanProcess() {
        addSubscription(interactor.scannedBoxes()
            .map { list ->
                if (list.isEmpty()) {
                    observeTimer()
                    CourierLoadingScanBoxState.Empty
                } else {
                    val lastBox = list.last()
                    CourierLoadingScanBoxState.BoxInit(
                        lastBox.id,
                        lastBox.address,
                        resourceProvider.getAccepted(list.size),
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
        addSubscription(
            interactor.scanLoaderProgress()
                .subscribe({
                    _progressEvent.value = when (it) {
                        CourierLoadingProgressData.Complete -> {
                            CourierLoadingScanProgress.LoaderComplete
                        }
                        CourierLoadingProgressData.Progress -> {
                            CourierLoadingScanProgress.LoaderProgress
                        }
                    }
                }, {})
        )
    }

    private fun observeScanProcessComplete(scanProcess: CourierLoadingProcessData) {
        LogUtils { logDebugApp("observeScanProcessComplete " + scanProcess) }
        val scanBoxData = scanProcess.scanBoxData
        val accepted = resourceProvider.getAccepted(scanProcess.count)
        when (scanBoxData) {
            is CourierLoadingScanBoxData.BoxAdded -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierLoadingScanBoxState.BoxAdded(qrCode, address, accepted)
                }
                _beepEvent.value = CourierLoadingScanBeepState.BoxAdded
                _orderTimer.value = CourierLoadingScanTimerState.Stopped
                _bottomEvent.value = CourierLoadingScanBottomState.Enable
            }
            is CourierLoadingScanBoxData.UnknownBox -> {
                _navigationEvent.value = CourierLoadingScanNavAction.NavigateToUnknownBox
                _boxStateUI.value = CourierLoadingScanBoxState.UnknownBox(
                    resourceProvider.getEmptyQr(),
                    resourceProvider.getEmptyAddress(),
                    accepted
                )
                _beepEvent.value = CourierLoadingScanBeepState.UnknownBox
                _bottomEvent.value =
                    if (scanProcess.count > 0) CourierLoadingScanBottomState.Enable else CourierLoadingScanBottomState.Disable
            }
            CourierLoadingScanBoxData.Empty -> _boxStateUI.value = CourierLoadingScanBoxState.Empty
        }
    }

    fun onListClicked() {
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToBoxes
    }

    fun onCompleteClicked() {
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToConfirmDialog
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

    override fun onTimerState(duration: Int, downTickSec: Int) {
        updateTimer(duration, downTickSec)
    }

    private fun updateTimer(duration: Int, downTickSec: Int) {
        _orderTimer.value =
            CourierLoadingScanTimerState.Timer(
                DateTimeFormatter.getAnalogTime(duration, downTickSec),
                DateTimeFormatter.getDigitTime(downTickSec)
            )
    }

    override fun onTimeIsOverState() {
        _orderTimer.value = CourierLoadingScanTimerState.TimeIsOut(
            DialogStyle.WARNING.ordinal,
            "Время вышло",
            "К сожалению, вы не успели приехать вовремя. Заказ был отменён",
            "Вернуться к списку заказов"
        )
    }


    fun returnToListOrderClick() {
        deleteTask()
    }

    private fun deleteTask() {
        addSubscription(interactor.deleteTask().subscribe(
            { toWarehouse() }, {})
        )
    }

    private fun toWarehouse() {
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToWarehouse
    }

}