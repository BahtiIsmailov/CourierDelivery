package ru.wb.go.ui.courierloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateHandler
import ru.wb.go.ui.courierloading.domain.*
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.time.DateTimeFormatter
import java.util.concurrent.TimeUnit

class CourierLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierLoadingResourceProvider,
    private val interactor: CourierLoadingInteractor,
    private val courierOrderTimerInteractor: CourierOrderTimerInteractor,
    private val deviceManager: DeviceManager,
) : TimerStateHandler, NetworkViewModel(compositeDisposable, metric) {

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

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigateToEmptyMessage = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToEmptyDialog: LiveData<NavigateToDialogInfo>
        get() = _navigateToEmptyMessage

    private val _navigateToErrorMessage = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToErrorMessage: LiveData<NavigateToDialogInfo>
        get() = _navigateToErrorMessage

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

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

    init {
        observeNetworkState()
        fetchVersionApp()
        observeInitScanProcess()
        observeScanProcess()
        observeScanProgress()
        getGate()
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun getGate() {
        addSubscription(
            interactor.info().subscribe(
                {
                    _orderTimer.value =
                        CourierLoadingScanTimerState.Info(if (it.gate.isEmpty()) "-" else it.gate)
                },
                { _orderTimer.value = CourierLoadingScanTimerState.Info("-") })
        )
    }

    private fun observeTimer() {
        addSubscription(
            courierOrderTimerInteractor.timer
                .subscribe({ observeTimerComplete(it) }, { observeTimerError(it) })
        )
    }

    private fun observeTimerComplete(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun observeTimerError(throwable: Throwable) {
        onTechErrorLog("observeTimerError", throwable)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    fun onConfirmLoadingClick() {
        onTechEventLog("onConfirmLoadingClick")
        onStopScanner()
        _progressEvent.value = CourierLoadingScanProgress.LoaderProgress
        addSubscription(
            interactor.confirmLoadingBoxes()
                .subscribe({ confirmLoadingBoxesComplete(it) }, { confirmLoadingBoxesError(it) })
        )
    }

    private fun confirmLoadingBoxesComplete(courierCompleteData: CourierCompleteData) {
        onTechEventLog(
            "confirmLoadingBoxesComplete",
            "loading box: " + courierCompleteData.countBox
        )
        _progressEvent.value = CourierLoadingScanProgress.LoaderComplete
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToIntransit(
            courierCompleteData.amount,
            courierCompleteData.countBox
        )
    }

    private fun confirmLoadingBoxesError(it: Throwable) {
        onTechErrorLog("confirmLoadingBoxesError", it)
        _progressEvent.value = CourierLoadingScanProgress.LoaderComplete
//        onStartScanner()
        confirmLoadingError(it)
    }

    private fun confirmLoadingError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                "Интернет-соединение отсутствует",
                throwable.message,
                "Понятно"
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.error.message,
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigateToErrorMessage.value = message
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
                { initScanProcessComplete(it) },
                { initScanProcessError(it) }
            )
        )
    }

    private fun initScanProcessComplete(it: CourierLoadingScanBoxState) {
        onTechEventLog("initScanProcessError", it.toString())
        _boxStateUI.value = it
    }

    private fun initScanProcessError(it: Throwable) {
        onTechErrorLog("initScanProcessError", it)
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
        onTechEventLog("observeScanProcessComplete", scanProcess.toString())
        val scanBoxData = scanProcess.scanBoxData
        val accepted = resourceProvider.getAccepted(scanProcess.count)
        when (scanBoxData) {
            is CourierLoadingScanBoxData.BoxFirstAdded -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierLoadingScanBoxState.BoxAdded(qrCode, address, accepted)
                }
                _beepEvent.value = CourierLoadingScanBeepState.BoxFirstAdded
                _orderTimer.value = CourierLoadingScanTimerState.Stopped
            }
            is CourierLoadingScanBoxData.BoxAdded -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierLoadingScanBoxState.BoxAdded(qrCode, address, accepted)
                }
                _beepEvent.value = CourierLoadingScanBeepState.BoxAdded
            }
            is CourierLoadingScanBoxData.UnknownBox -> {
                if (scanProcess.count == 0) {
                    _boxStateUI.value = CourierLoadingScanBoxState.UnknownBoxTimer
                } else {
                    _boxStateUI.value = CourierLoadingScanBoxState.UnknownBox(
                        scanBoxData.qrCode,
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                }
                _navigationEvent.value = CourierLoadingScanNavAction.NavigateToUnknownBox
                _beepEvent.value = CourierLoadingScanBeepState.UnknownBox
            }
            CourierLoadingScanBoxData.Empty -> _boxStateUI.value = CourierLoadingScanBoxState.Empty
            is CourierLoadingScanBoxData.UnknownQr -> {
                onStopScanner()
                _navigateToDialogInfo.value = NavigateToDialogInfo(
                    DialogInfoStyle.ERROR.ordinal,
                    resourceProvider.getScanDialogTitle(),
                    resourceProvider.getScanDialogMessage(),
                    resourceProvider.getScanDialogButton()
                )
            }
        }
    }

    fun onListClicked() {
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToBoxes
    }

    fun onCancelLoadingClick() {
        onTechEventLog("onCancelLoadingClick")
        onStartScanner()
    }

    fun onCompleteLoaderClicked() {
        onTechEventLog("onCompleteLoaderClicked", "NavigateToConfirmDialog")
        onStopScanner()
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToConfirmDialog
    }

    private fun observeScanProcessError(throwable: Throwable) {
        onTechErrorLog("observeScanProcessError", throwable)
        val error = if (throwable is CompositeException) {
            throwable.exceptions[0]
        } else throwable
        scanProcessError(error)
    }

    private fun scanProcessError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> {
                NavigateToDialogInfo(
                    DialogInfoStyle.WARNING.ordinal,
                    resourceProvider.getGenericInternetTitleError(),
                    resourceProvider.getGenericInternetMessageError(),
                    resourceProvider.getGenericInternetButtonError()
                )
            }
            is BadRequestException ->
                NavigateToDialogInfo(
                    DialogInfoStyle.ERROR.ordinal,
                    resourceProvider.getGenericServiceTitleError(),
                    throwable.error.message,
                    resourceProvider.getGenericServiceButtonError()
                )

            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }

        // TODO: 07.10.2021 привести диалог
        interactor.scannerAction(ScannerState.Stop)
        _navigateToEmptyMessage.value = message
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

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
        onTechEventLog("onTimeIsOverState")
        _orderTimer.value = CourierLoadingScanTimerState.TimeIsOut(
            DialogInfoStyle.WARNING.ordinal,
            resourceProvider.getScanDialogTimeIsOutTitle(),
            resourceProvider.getScanDialogTimeIsOutMessage(),
            resourceProvider.getScanDialogTimeIsOutButton()
        )
    }

    fun returnToListOrderClick() {
        onTechEventLog("returnToListOrderClick")
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

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierLoadingScan"
    }

}