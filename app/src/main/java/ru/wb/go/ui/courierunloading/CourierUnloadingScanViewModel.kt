package ru.wb.go.ui.courierunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractor
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingProcessData
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingProgressData
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingScanBoxData
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import java.util.concurrent.TimeUnit

class CourierUnloadingScanViewModel(
    private val parameters: CourierUnloadingScanParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierUnloadingResourceProvider,
    private val interactor: CourierUnloadingInteractor,
    private val deviceManager: DeviceManager,
    private val metric: YandexMetricManager,
) : NetworkViewModel(compositeDisposable) {
    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigationEvent =
        SingleLiveEvent<CourierUnloadingScanNavAction>()
    val navigationEvent: LiveData<CourierUnloadingScanNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _navigateToDialogConfirmInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmInfo

    private val _beepEvent =
        SingleLiveEvent<CourierUnloadingScanBeepState>()
    val beepEvent: LiveData<CourierUnloadingScanBeepState>
        get() = _beepEvent

    private val _progressEvent =
        SingleLiveEvent<CourierUnloadingScanProgress>()
    val progressEvent: LiveData<CourierUnloadingScanProgress>
        get() = _progressEvent

    private val _boxStateUI =
        MutableLiveData<CourierUnloadingScanBoxState>()
    val boxStateUI: LiveData<CourierUnloadingScanBoxState>
        get() = _boxStateUI

    private val _bottomEvent =
        MutableLiveData<CourierUnloadingScanBottomState>()
    val bottomProgressEvent: LiveData<CourierUnloadingScanBottomState>
        get() = _bottomEvent

    init {
        initToolbar()
        fetchVersionApp()
        observeNetworkState()
        observeInitScanProcess()
        observeScanProcess()
        observeScanProgress()
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun observeInitScanProcess() {
        addSubscription(interactor.readUnloadingLastBox(parameters.officeId)
            .map { initResult ->
                val readyStatus = resourceProvider.getReadyStatus()
                val accepted = resourceProvider.getAccepted(
                    initResult.deliveredCount,
                    initResult.fromCount
                )
                if (initResult.id.isEmpty()) {
                    CourierUnloadingScanBoxState.Empty(
                        readyStatus,
                        resourceProvider.getEmptyQr(),
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                } else {
                    CourierUnloadingScanBoxState.BoxInit(
                        readyStatus, initResult.id, initResult.address, accepted
                    )
                }
            }.subscribe(
                { _boxStateUI.value = it },
                { LogUtils { logDebugApp(it.toString()) } }
            )
        )
    }

    private fun initToolbar() {
        addSubscription(
            interactor.nameOffice(parameters.officeId)
                .subscribe({ _toolbarLabelState.value = Label(it) },
                    {})
        )
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    fun onCancelUnloadingClick() {
        onStartScanner()
    }

    fun onConfirmUnloadingClick() {
        metric.onTechUIEventLog(
            SCREEN_TAG, "onConfirmUnloadingClick", "confirmUnloading"
        )
        confirmUnloading()
    }

    private fun confirmUnloading() {
        _progressEvent.value = CourierUnloadingScanProgress.LoaderProgress
        addSubscription(
            interactor.confirmUnloading(parameters.officeId)
                .subscribe({ confirmUnloadingComplete() }, { confirmUnloadingError(it) })
        )
    }

    private fun confirmUnloadingComplete() {
        metric.onTechUIEventLog(
            SCREEN_TAG, "confirmUnloadingComplete", "navigate to intransit"
        )
        clearSubscription()
        _progressEvent.value = CourierUnloadingScanProgress.LoaderComplete
        _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToIntransit

    }

    private fun confirmUnloadingError(throwable: Throwable) {
        metric.onTechErrorLog(SCREEN_TAG, "confirmUnloadingError", throwable.toString())
        clearSubscription()
        _progressEvent.value = CourierUnloadingScanProgress.LoaderComplete
        _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToIntransit
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeScanProcess(parameters.officeId)
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
                        CourierUnloadingProgressData.Complete -> {
                            CourierUnloadingScanProgress.LoaderComplete
                        }
                        CourierUnloadingProgressData.Progress -> {
                            CourierUnloadingScanProgress.LoaderProgress
                        }
                    }
                }, {})
        )
    }

    private fun observeScanProcessComplete(scanProcess: CourierUnloadingProcessData) {
        metric.onTechUIEventLog(
            SCREEN_TAG, "observeScanProcessComplete", "scanProcess " + scanProcess
        )
        val scanBoxData = scanProcess.scanBoxData
        val accepted =
            resourceProvider.getAccepted(scanProcess.unloadingCounter, scanProcess.fromCounter)
        when (scanBoxData) {
            is CourierUnloadingScanBoxData.ScannerReady -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierUnloadingScanBoxState.ScannerReady(
                        resourceProvider.getReadyStatus(),
                        qrCode,
                        address,
                        accepted
                    )
                }
            }
            is CourierUnloadingScanBoxData.BoxAdded -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierUnloadingScanBoxState.BoxAdded(
                        resourceProvider.getReadyAddedBox(),
                        qrCode,
                        address,
                        accepted
                    )
                }
                _beepEvent.value = CourierUnloadingScanBeepState.BoxAdded
                _bottomEvent.value = CourierUnloadingScanBottomState.Enable
            }
            is CourierUnloadingScanBoxData.UnknownBox -> {
                _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToUnknownBox
                _boxStateUI.value = CourierUnloadingScanBoxState.UnknownBox(
                    resourceProvider.getReadyUnknownBox(),
                    if (scanBoxData.qrCode.isEmpty()) resourceProvider.getEmptyQr() else scanBoxData.qrCode,
                    if (scanBoxData.address.isEmpty()) resourceProvider.getEmptyAddress() else scanBoxData.address,
                    accepted
                )
                _beepEvent.value = CourierUnloadingScanBeepState.UnknownBox
            }
            CourierUnloadingScanBoxData.Empty -> _boxStateUI.value =
                CourierUnloadingScanBoxState.Empty(
                    resourceProvider.getReadyStatus(),
                    resourceProvider.getEmptyQr(),
                    resourceProvider.getEmptyAddress(),
                    accepted
                )
            is CourierUnloadingScanBoxData.UnloadingCompleted -> {
                _boxStateUI.value = with(scanBoxData) {
                    CourierUnloadingScanBoxState.BoxAdded(
                        resourceProvider.getReadyAddedBox(),
                        qrCode,
                        address,
                        accepted
                    )
                }
                _bottomEvent.value = CourierUnloadingScanBottomState.Enable
            }
        }
    }

    fun onListClicked() {
        _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToBoxes
    }

    fun onCompleteUnloadClicked() {
        onStopScanner()
        addSubscription(
            interactor.readUnloadingBoxCounter(parameters.officeId).subscribe({
                if (it.fromCount == it.unloadedCount) {
                    confirmUnloading()
                } else {
                    _navigateToDialogConfirmInfo.value = NavigateToDialogConfirmInfo(
                        DialogInfoStyle.ERROR.ordinal,
                        resourceProvider.getUnloadingDialogTitle(),
                        resourceProvider.getUnloadingDialogMessage(
                            it.unloadedCount,
                            it.fromCount
                        ),
                        resourceProvider.getUnloadingDialogPositive(),
                        resourceProvider.getUnloadingDialogNegative()
                    )
                }
            },
                { onStartScanner() })
        )
    }

    private fun observeScanProcessError(throwable: Throwable) {
        metric.onTechErrorLog(SCREEN_TAG, "observeScanProcessError", throwable.toString())
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
        onStopScanner()
        _beepEvent.value = CourierUnloadingScanBeepState.UnknownQR
        _navigateToDialogInfo.value = NavigateToDialogInfo(
            DialogInfoStyle.ERROR.ordinal,
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton()
        )
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    companion object {
        const val SCREEN_TAG = "CourierUnloadingScan"
    }

    data class Label(val label: String)

}