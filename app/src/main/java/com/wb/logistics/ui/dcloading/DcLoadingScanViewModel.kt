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
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.disposables.CompositeDisposable

class DcLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcLoadingResourceProvider,
    private val interactor: DcLoadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<DcLoadingScanNavAction>()
    val navigationEvent: LiveData<DcLoadingScanNavAction>
        get() = _navigationEvent

    private val _toastEvent =
        SingleLiveEvent<DcLoadingScanToastState>()
    val toastEvent: LiveData<DcLoadingScanToastState>
        get() = _toastEvent

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _beepEvent =
        SingleLiveEvent<DcLoadingScanBeepState>()
    val beepEvent: LiveData<DcLoadingScanBeepState>
        get() = _beepEvent

    val boxStateUI = MutableLiveData<DcLoadingScanBoxState>()

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        addSubscription(interactor.observeScannedBoxes().subscribe {
            boxStateUI.value = if (it.isEmpty()) DcLoadingScanBoxState.Empty
            else {
                val lastBox = it.last()
                DcLoadingScanBoxState.BoxInit(
                    it.size.toString(),
                    lastBox.gate.toString(),
                    lastBox.barcode)
            }
        })
        addSubscription(interactor.observeScanProcess()
            .subscribe(
                { addBoxToFlightComplete(it) },
                { addBoxToFlightError(it) }
            )
        )
    }

    private fun addBoxToFlightComplete(scanProcess: ScanProcessData) {
        val scanBoxData = scanProcess.scanBoxData
        val accepted = scanProcess.count.toString()
        when (scanBoxData) {
            is ScanBoxData.BoxAdded -> {
                boxStateUI.value = with(scanBoxData) {
                    DcLoadingScanBoxState.BoxAdded(accepted, gate, barcode)
                }
                _toastEvent.value = DcLoadingScanToastState.BoxAdded(
                    resourceProvider.getShortAddedBox(scanBoxData.barcode))
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
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxHasBeenAdded(
                            accepted,
                            gate,
                            barcode)
                    }
                _toastEvent.value =
                    DcLoadingScanToastState.BoxHasBeenAdded(resourceProvider.getShortHasBeenAddedBox(
                        scanBoxData.barcode))
            }
            ScanBoxData.Empty -> boxStateUI.value = DcLoadingScanBoxState.Empty
            is ScanBoxData.BoxDoesNotBelongInfoEmpty -> {
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongInfoTitle(),
                        scanBoxData.barcode,
                        resourceProvider.getBoxNotBelongAddress())
            }
        }
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.boxScanned(barcode, true)
    }

    fun onListClicked() {
        _navigationEvent.value = DcLoadingScanNavAction.NavigateToBoxes
    }

    fun onCompleteClicked() {
        bottomProgressEvent.value = true
        addSubscription(interactor.switchScreen().subscribe(
            {
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToFlightDeliveries
                bottomProgressEvent.value = false
            },
            {
                // TODO: 30.05.2021 реализовать сообщение
                bottomProgressEvent.value = false
            })
        )
    }

    private fun addBoxToFlightError(throwable: Throwable) {
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