package ru.wb.go.ui.scanner

import androidx.lifecycle.LiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.PREFIX_QR_CODE
import ru.wb.go.app.PREFIX_QR_OFFICE_CODE
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils

class CourierScannerViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: ScannerInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _scannerAction = SingleLiveEvent<ScannerState>()
    val scannerAction: LiveData<ScannerState>
        get() = _scannerAction

    init {
        addSubscription(interactor.observeScannerState().subscribe { _scannerAction.value = it })
    }

    fun onBarcodeScanned(barcode: String) {
        LogUtils { logDebugApp("onBarcodeScanned(barcode: String) " + barcode) }
        if (barcode.startsWith(PREFIX_QR_CODE)
            || barcode.uppercase().startsWith(PREFIX_QR_OFFICE_CODE)
        ) {
            _scannerAction.value = ScannerState.BeepScan
            interactor.barcodeScanned(barcode)
        }
    }

}