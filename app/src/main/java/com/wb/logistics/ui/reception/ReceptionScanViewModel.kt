package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.nav.domain.ScreenManager
import com.wb.logistics.ui.nav.domain.ScreenManagerState
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.ui.reception.domain.ScanBoxData
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable

class ReceptionScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: ReceptionScanResourceProvider,
    private val interactor: ReceptionInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<ReceptionScanNavAction>()
    val navigationEvent: LiveData<ReceptionScanNavAction>
        get() = _navigationEvent
    private val _toastEvent =
        SingleLiveEvent<ReceptionScanToastState<Nothing>>()
    val toastEvent: LiveData<ReceptionScanToastState<Nothing>>
        get() = _toastEvent
    private val _beepEvent =
        SingleLiveEvent<ReceptionScanBeepState<Nothing>>()
    val beepEvent: LiveData<ReceptionScanBeepState<Nothing>>
        get() = _beepEvent

    val boxStateUI = MutableLiveData<ReceptionScanBoxState<Nothing>>()

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        //addMockScannedBox()
        //screenManager.saveScreenState(ScreenState.RECEPTION_SCAN)
        addSubscription(interactor.observeScannedBoxes().subscribe {
            if (it.isEmpty()) {
                boxStateUI.value = ReceptionScanBoxState.Empty
            } else {
                val lastBox = it.last()
                boxStateUI.value =
                    ReceptionScanBoxState.BoxInit(
                        it.size.toString(),
                        lastBox.gate.toString(),
                        lastBox.barcode)
            }
        })

        addSubscription(
            interactor.observeScanProcess()
                .flatMapSingle { data ->
                    interactor.readBoxesScanned()
                        .map { Pair(data, it) }
                }.subscribe { addBoxToFlightComplete(it) }
        )
    }

    private fun addMockScannedBox() {
        addSubscription(interactor.addMockScannedBox()
            .subscribe({ LogUtils { logDebugApp("receptionInteractor.addMockScannedBox() complete") } },
                { LogUtils { logDebugApp("receptionInteractor.addMockScannedBox() error") } }))
    }

    private fun addBoxToFlightComplete(pair: Pair<ScanBoxData, List<AttachedBoxEntity>>) {
        val scanBoxData = pair.first
        val scannedBoxes = pair.second
        val accepted = scannedBoxes.size.toString()
        when (scanBoxData) {
            is ScanBoxData.BoxAdded -> {
                boxStateUI.value = with(scanBoxData) {
                    ReceptionScanBoxState.BoxAdded(accepted, gate, barcode)
                }
                _toastEvent.value =
                    ReceptionScanToastState.BoxAdded(resourceProvider.getShortAddedBox(
                        scanBoxData.barcode))
                _beepEvent.value = ReceptionScanBeepState.BoxAdded
            }
            is ScanBoxData.BoxDoesNotBelongDc -> {
                _navigationEvent.call()
                _navigationEvent.value =
                    ReceptionScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongDcToolbarTitle(),
                        resourceProvider.getBoxNotBelongDcTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionScanBoxState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
            }
            is ScanBoxData.BoxDoesNotBelongFlight -> {
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    ReceptionScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongFlightToolbarTitle(),
                        resourceProvider.getBoxNotBelongFlightTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionScanBoxState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
            }
            is ScanBoxData.BoxDoesNotBelongGate -> {
                // TODO: 07.04.2021
            }
            is ScanBoxData.BoxHasBeenAdded -> {
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionScanBoxState.BoxHasBeenAdded(
                            accepted,
                            gate,
                            barcode)
                    }
                _toastEvent.value =
                    ReceptionScanToastState.BoxHasBeenAdded(resourceProvider.getShortHasBeenAddedBox(
                        scanBoxData.barcode))
            }
            ScanBoxData.Empty -> boxStateUI.value = ReceptionScanBoxState.Empty
            is ScanBoxData.BoxDoesNotBelongInfo -> {
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    ReceptionScanNavAction.NavigateToReceptionBoxNotBelong(
                        resourceProvider.getBoxNotBelongFlightToolbarTitle(),
                        resourceProvider.getBoxNotBelongFlightTitle(),
                        scanBoxData.barcode,
                        resourceProvider.getBoxNotBelongAddress())
            }
        }
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.boxScanned(barcode.replace("-", ""), true)
    }

    fun onListClicked() {
        _navigationEvent.value = ReceptionScanNavAction.NavigateToBoxes
    }

    fun onCompleteClicked() {
        toFlightDeliveries()
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerAction.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerAction.Start)
    }

    private fun toFlightDeliveries() {
        bottomProgressEvent.value = true
        addSubscription(interactor.sendAwaitBoxes().subscribe({
            if (it > 0) {
                bottomProgressEvent.value = false
            } else {
                screenManager.saveScreenState(ScreenManagerState.FlightPickUpPoint)
                _navigationEvent.value = ReceptionScanNavAction.NavigateToFlightDeliveries
                bottomProgressEvent.value = false
            }

        }, {
            bottomProgressEvent.value = false
        }))
    }

}