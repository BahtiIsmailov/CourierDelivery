package com.wb.logistics.ui.dcloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcloading.domain.DcLoadingInteractor
import com.wb.logistics.ui.dcloading.domain.ScanBoxData
import com.wb.logistics.ui.dcloading.domain.ScanProcessData
import com.wb.logistics.ui.dcloading.domain.ScanProgressData
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import java.util.concurrent.TimeUnit

class DcLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcLoadingResourceProvider,
    private val interactor: DcLoadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<DcLoadingScanNavAction>()
    val navigationEvent: LiveData<DcLoadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _beepEvent =
        SingleLiveEvent<DcLoadingScanBeepState>()
    val beepEvent: LiveData<DcLoadingScanBeepState>
        get() = _beepEvent

    private val _progressEvent =
        SingleLiveEvent<DcLoadingScanProgress>()
    val progressEvent: LiveData<DcLoadingScanProgress>
        get() = _progressEvent

    val boxStateUI = MutableLiveData<DcLoadingScanBoxState>()

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        observeInitScanProcess()
        observeScanProcess()
        observeScanProgress()
    }

    private fun observeInitScanProcess() {
        addSubscription(Observable.combineLatest(interactor.gate().toObservable(),
            interactor.observeScannedBoxes(),
            { gate, list ->
                if (list.isEmpty()) DcLoadingScanBoxState.Empty
                else {
                    val lastBox = list.last()
                    DcLoadingScanBoxState.BoxInit(
                        list.size.toString(),
                        gate,
                        lastBox.barcode)
                }

            }).subscribe { boxStateUI.value = it })
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
                    ScanProgressData.Complete -> DcLoadingScanProgress.LoaderComplete
                    ScanProgressData.Progress -> DcLoadingScanProgress.LoaderProgress
                }
            })
    }

    private fun observeScanProcessComplete(scanProcess: ScanProcessData) {
        val scanBoxData = scanProcess.scanBoxData
        val accepted = scanProcess.count.toString()
        when (scanBoxData) {
            is ScanBoxData.BoxAdded -> {
                boxStateUI.value = with(scanBoxData) {
                    DcLoadingScanBoxState.BoxAdded(accepted, gate, barcode)
                }
                _beepEvent.value = DcLoadingScanBeepState.BoxAdded
            }
            is ScanBoxData.BoxDoesNotBelongDc -> {
                _navigationEvent.call()
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongDcTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxDeny(
                            accepted,
                            resourceProvider.getEmptyGate(),
                            barcode)
                    }
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
            }
            is ScanBoxData.BoxDoesNotBelongFlight -> {
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongFlightTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxDeny(
                            accepted,
                            resourceProvider.getEmptyGate(),
                            barcode)
                    }
            }
            is ScanBoxData.BoxHasBeenAdded -> {
                _beepEvent.value = DcLoadingScanBeepState.BoxAdded
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxHasBeenAdded(
                            accepted,
                            gate,
                            barcode)
                    }
            }
            ScanBoxData.Empty -> boxStateUI.value = DcLoadingScanBoxState.Empty
            is ScanBoxData.BoxDoesNotBelongInfoEmpty -> {
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongInfoTitle(),
                        scanBoxData.barcode,
                        resourceProvider.getBoxNotBelongAddress())
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxDeny(
                            accepted,
                            resourceProvider.getEmptyGate(),
                            barcode)
                    }
            }
        }
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.barcodeManualInput(barcode)
    }

    fun onListClicked() {
        _navigationEvent.value = DcLoadingScanNavAction.NavigateToBoxes
    }

    fun onCompleteClicked() {
        bottomProgressEvent.value = true
        addSubscription(interactor.switchScreen().subscribe(
            {
                _navigationEvent.value = DcLoadingScanNavAction.NavigateToFlightDeliveries
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
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton())
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
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton())
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerAction.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerAction.Start)
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}