package ru.wb.perevozka.ui.courierunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierunloading.domain.CourierUnloadingInteractor
import ru.wb.perevozka.ui.courierunloading.domain.CourierUnloadingProcessData
import ru.wb.perevozka.ui.courierunloading.domain.CourierUnloadingProgressData
import ru.wb.perevozka.ui.courierunloading.domain.CourierUnloadingScanBoxData
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.utils.LogUtils
import java.util.concurrent.TimeUnit

class CourierUnloadingScanViewModel(
    private val parameters: CourierUnloadingScanParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierUnloadingResourceProvider,
    private val interactor: CourierUnloadingInteractor,

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

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

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
        observeNetworkState()
        observeInitScanProcess()
        observeScanProcess()
        observeScanProgress()
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

    fun cancelUnloadingClick() {
        onStartScanner()
    }

    fun confirmUnloadingClick() {
        _progressEvent.value = CourierUnloadingScanProgress.LoaderProgress
        addSubscription(
            interactor.confirmUnloading(parameters.officeId)
                .subscribe({ confirmUnloadingComplete() }, { confirmUnloadingError() })
        )
    }

    private fun confirmUnloadingComplete() {
        LogUtils { logDebugApp("confirmUnloadingComplete") }
        clearSubscription()
        _progressEvent.value = CourierUnloadingScanProgress.LoaderComplete
        _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToIntransit

    }

    private fun confirmUnloadingError() {
        LogUtils { logDebugApp("confirmUnloadingError") }
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
        LogUtils { logDebugApp("Unloading Observe Scan Process Complete " + scanProcess) }
        val scanBoxData = scanProcess.scanBoxData
        val accepted =
            resourceProvider.getAccepted(scanProcess.unloadingCounter, scanProcess.fromCounter)
        when (scanBoxData) {
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
//                _bottomEvent.value =
//                    if (scanProcess.unloadingCounter > 0) CourierUnloadingScanBottomState.Enable else CourierUnloadingScanBottomState.Disable
            }
            CourierUnloadingScanBoxData.Empty -> _boxStateUI.value =
                CourierUnloadingScanBoxState.Empty(
                    resourceProvider.getReadyStatus(),
                    resourceProvider.getEmptyQr(),
                    resourceProvider.getEmptyAddress(),
                    accepted
                )
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
            is CourierUnloadingScanBoxData.UnloadingCompleted -> {
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
        }
    }

    fun onListClicked() {
        _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToBoxes
    }

    fun onCompleteUnloadClicked() {
        onStopScanner()
        addSubscription(
            interactor.readUnloadingBoxCounter(parameters.officeId).subscribe({
                _navigationEvent.value = CourierUnloadingScanNavAction.NavigateToConfirmDialog(
                    resourceProvider.getUnloadingDialogTitle(),
                    resourceProvider.getUnloadingDialogMessage(it.unloadedCount, it.fromCount)
                )
            },
                { onStartScanner() })
        )
    }

    private fun observeScanProcessError(throwable: Throwable) {
        LogUtils { logDebugApp("observeScanProcessError " + throwable) }
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
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            DialogStyle.ERROR.ordinal,
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton()
        )
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    data class Label(val label: String)

    data class NavigateToMessageInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    )

}