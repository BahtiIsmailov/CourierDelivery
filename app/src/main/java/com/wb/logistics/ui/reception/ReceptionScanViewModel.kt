package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.nav.domain.ScreenManager
import com.wb.logistics.ui.nav.domain.ScreenState
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.ui.reception.domain.ScanBoxData
import com.wb.logistics.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class ReceptionScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val receptionResourceProvider: ReceptionResourceProvider,
    private val receptionInteractor: ReceptionInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<ReceptionScanNavigationEvent>()
    val navigationEvent: LiveData<ReceptionScanNavigationEvent>
        get() = _navigationEvent
    private val _toastEvent =
        SingleLiveEvent<ReceptionScanToastState<Nothing>>()
    val toastEvent: LiveData<ReceptionScanToastState<Nothing>>
        get() = _toastEvent
    private val _beepEvent =
        SingleLiveEvent<ReceptionScanBeepState<Nothing>>()
    val beepEvent: LiveData<ReceptionScanBeepState<Nothing>>
        get() = _beepEvent

    val boxStateUI = MutableLiveData<ReceptionScanBoxUIState<Nothing>>()

    val bottomNavigationEvent = MutableLiveData<Boolean>()

    init {
        //addMockScannedBox()
        addSubscription(
            Observable.combineLatest(
                receptionInteractor.observeScanProcess().startWith(ScanBoxData.Init),
                receptionInteractor.observeScannedBoxes(),
                { scanState, scannedBoxes -> Pair(scanState, scannedBoxes) })
                .subscribe { addBoxToFlightComplete(it) }
        )
    }

    private fun addMockScannedBox() {
        addSubscription(receptionInteractor.addMockScannedBox()
            .subscribe({ LogUtils { logDebugApp("receptionInteractor.addMockScannedBox() complete") } },
                { LogUtils { logDebugApp("receptionInteractor.addMockScannedBox() error") } }))
    }

    private fun addBoxToFlightComplete(pair: Pair<ScanBoxData, List<ScannedBoxEntity>>) {
        val scanBoxData = pair.first
        val scannedBoxes = pair.second
        val accepted = scannedBoxes.size.toString()
        bottomNavigationEvent.value = scannedBoxes.isNotEmpty()
        when (scanBoxData) {
            ScanBoxData.Init -> {
                if (scannedBoxes.isEmpty()) {
                    boxStateUI.value = ReceptionScanBoxUIState.Empty
                } else {
                    val lastBox = scannedBoxes.last()
                    boxStateUI.value =
                        ReceptionScanBoxUIState.BoxInit(
                            accepted,
                            lastBox.gate.toString(),
                            lastBox.barcode)
                }
            }
            is ScanBoxData.BoxAdded -> {
                boxStateUI.value = with(scanBoxData) {
                    ReceptionScanBoxUIState.BoxAdded(accepted, gate, barcode)
                }
                _toastEvent.value =
                    ReceptionScanToastState.BoxAdded(receptionResourceProvider.getShortAddedBox(
                        scanBoxData.barcode))
            }
            is ScanBoxData.BoxDoesNotBelongDc -> {
                _navigationEvent.call()
                _navigationEvent.value =
                    ReceptionScanNavigationEvent.NavigateToReceptionBoxNotBelong(
                        receptionResourceProvider.getBoxNotBelongDcToolbarTitle(),
                        receptionResourceProvider.getBoxNotBelongDcTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionScanBoxUIState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
            }
            is ScanBoxData.BoxDoesNotBelongFlight -> {
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    ReceptionScanNavigationEvent.NavigateToReceptionBoxNotBelong(
                        receptionResourceProvider.getBoxNotBelongFlightToolbarTitle(),
                        receptionResourceProvider.getBoxNotBelongFlightTitle(),
                        scanBoxData.barcode,
                        scanBoxData.address)
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionScanBoxUIState.BoxDeny(
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
                        ReceptionScanBoxUIState.BoxHasBeenAdded(
                            accepted,
                            gate,
                            barcode)
                    }
                _toastEvent.value =
                    ReceptionScanToastState.BoxHasBeenAdded(receptionResourceProvider.getShortHasBeenAddedBox(
                        scanBoxData.barcode))
            }
            ScanBoxData.Empty -> boxStateUI.value = ReceptionScanBoxUIState.Empty
            is ScanBoxData.BoxDoesNotBelongInfo -> {
                _beepEvent.value = ReceptionScanBeepState.BoxSkipAdded
                _navigationEvent.value =
                    ReceptionScanNavigationEvent.NavigateToReceptionBoxNotBelong(
                        receptionResourceProvider.getBoxNotBelongFlightToolbarTitle(),
                        receptionResourceProvider.getBoxNotBelongFlightTitle(),
                        scanBoxData.barcode,
                        receptionResourceProvider.getBoxNotBelongAddress())
            }
        }
    }

    fun onBoxHandleInput(barcode: String) {
        receptionInteractor.boxScanned(barcode.replace("-", ""), true)
    }

    fun onBoxScanned(barcode: String) {
        receptionInteractor.boxScanned(receptionResourceProvider.getBarCodeBox(barcode), false)
    }

    fun onListClicked() {
        _navigationEvent.value = ReceptionScanNavigationEvent.NavigateToBoxes
    }

    fun onCompleteClicked() {
        screenManager.saveScreenState(ScreenState.FLIGHT_PICK_UP_POINT)
        _navigationEvent.value = ReceptionScanNavigationEvent.NavigateToFlightDeliveries
    }

}