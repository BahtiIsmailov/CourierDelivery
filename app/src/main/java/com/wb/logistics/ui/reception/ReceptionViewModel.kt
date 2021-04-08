package com.wb.logistics.ui.reception

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.ui.reception.domain.ScanBoxData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class ReceptionViewModel(
    compositeDisposable: CompositeDisposable,
    private val receptionResourceProvider: ReceptionResourceProvider,
    private val receptionInteractor: ReceptionInteractor,
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<ReceptionUIState<Nothing>>()

    val boxStateUI = MutableLiveData<ReceptionBoxUIState<Nothing>>()

    val navigationStateUI = MutableLiveData<Boolean>()

    init {
        addSubscription(
            Observable.combineLatest(
                receptionInteractor.observeScanState().startWith(ScanBoxData.Init),
                receptionInteractor.observeFlightBoxes(),
                { scanState, flightBoxes -> Pair(scanState, flightBoxes) })
                .subscribe { addBoxToFlightComplete(it) }
        )
    }

    fun onBoxHandleInput(barcode: String) {
        receptionInteractor.boxScanned(barcode.replace("-", ""), true)
    }

    fun onBoxScanned(barcode: String) {
        receptionInteractor.boxScanned(receptionResourceProvider.getBarCodeBox(barcode), false)
    }

    private fun addBoxToFlightComplete(pair: Pair<ScanBoxData, List<FlightBoxScannedEntity>>) {
        val scanBoxData = pair.first
        val scannedBoxes = pair.second
        val accepted = scannedBoxes.size.toString()
        navigationStateUI.value = scannedBoxes.isNotEmpty()
        when (scanBoxData) {
            ScanBoxData.Init -> {
                if (scannedBoxes.isEmpty()) {
                    boxStateUI.value = ReceptionBoxUIState.Empty
                } else {
                    val lastBox = scannedBoxes.last()
                    boxStateUI.value =
                        ReceptionBoxUIState.BoxInit(
                            accepted,
                            lastBox.gate.toString(),
                            lastBox.barcode)
                }
            }
            is ScanBoxData.BoxAdded -> {
                boxStateUI.value = with(scanBoxData) {
                    ReceptionBoxUIState.BoxComplete(
                        receptionResourceProvider.getShortAddedBox(barcode),
                        accepted,
                        gate,
                        barcode)
                }
            }
            is ScanBoxData.BoxDoesNotBelongDc -> {
                stateUI.value = ReceptionUIState.NavigateToReceptionBoxNotBelong(
                    receptionResourceProvider.getBoxNotBelongDcToolbarTitle(),
                    receptionResourceProvider.getBoxNotBelongDcTitle(),
                    scanBoxData.barcode,
                    scanBoxData.address)
                stateUI.value = ReceptionUIState.Empty
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionBoxUIState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
            }
            is ScanBoxData.BoxDoesNotBelongFlight -> {
                stateUI.value = ReceptionUIState.NavigateToReceptionBoxNotBelong(
                    receptionResourceProvider.getBoxNotBelongFlightToolbarTitle(),
                    receptionResourceProvider.getBoxNotBelongFlightTitle(),
                    scanBoxData.barcode,
                    scanBoxData.address)
                stateUI.value = ReceptionUIState.Empty
                boxStateUI.value =
                    with(scanBoxData) {
                        ReceptionBoxUIState.BoxDeny(
                            accepted,
                            gate,
                            barcode)
                    }
            }
            is ScanBoxData.BoxDoesNotBelongGate -> {
                // TODO: 07.04.2021
            }
            is ScanBoxData.BoxHasBeenAdded -> {
                boxStateUI.value =
                    with(scanBoxData) {
                        val toastBox = receptionResourceProvider.getShortHasBeenAddedBox(barcode)
                        ReceptionBoxUIState.BoxHasBeenAdded(
                            toastBox,
                            accepted,
                            gate,
                            barcode)
                    }
            }
            ScanBoxData.Empty -> boxStateUI.value = ReceptionBoxUIState.Empty

        }
    }

    fun onListClicked() {
        stateUI.value = ReceptionUIState.NavigateToBoxes
        stateUI.value = ReceptionUIState.Empty
    }

}