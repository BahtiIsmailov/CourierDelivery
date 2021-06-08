package com.wb.logistics.ui.dcloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcloading.domain.DcLoadingInteractor
import com.wb.logistics.ui.dcloading.domain.ScanBoxData
import com.wb.logistics.ui.dcloading.domain.ScanProcessData
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.disposables.CompositeDisposable

class DcLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcLoadingScanResourceProvider,
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

    private val _beepEvent =
        SingleLiveEvent<DcLoadingScanBeepState>()
    val beepEvent: LiveData<DcLoadingScanBeepState>
        get() = _beepEvent

    val boxStateUI = MutableLiveData<DcLoadingScanBoxState>()

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        // TODO: 19.05.2021 addMockScannedBox
        //addMockScannedBox()
        // TODO: 08.06.2021 инициализация виджетов
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

        addSubscription(interactor.observeScanProcess().subscribe { addBoxToFlightComplete(it) })
    }

//    private fun addMockScannedBox() {
//        addSubscription(interactor.addMockScannedBox()
//            .subscribe({ LogUtils { logDebugApp("receptionInteractor.addMockScannedBox() complete") } },
//                { LogUtils { logDebugApp("receptionInteractor.addMockScannedBox() error") } }))
//    }

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
                        resourceProvider.getBoxNotBelongDcToolbarTitle(),
                        resourceProvider.getBoxNotBelongDcTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
            }
            is ScanBoxData.BoxDoesNotBelongFlight -> {
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongFlightToolbarTitle(),
                        resourceProvider.getBoxNotBelongFlightTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        DcLoadingScanBoxState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
            }
            is ScanBoxData.BoxDoesNotBelongGate -> {
                // TODO: 07.04.2021 Не принадлежит Gate реализовать
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
            is ScanBoxData.BoxDoesNotBelongInfo -> {
                _beepEvent.value = DcLoadingScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongFlightToolbarTitle(),
                        resourceProvider.getBoxNotBelongFlightTitle(),
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
        toFlightDeliveries()
    }

    private fun toFlightDeliveries() {
        bottomProgressEvent.value = true
        addSubscription(interactor.sendAwaitBoxesCount().subscribe(
            {
                if (it > 0) {
                    // TODO: 30.05.2021 Добавить сообщение что коробки не были добавлены в базу
                    bottomProgressEvent.value = false
                } else {
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
            },
            {
                bottomProgressEvent.value = false
            })
        )
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerAction.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerAction.Start)
    }

}