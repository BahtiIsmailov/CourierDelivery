package ru.wb.go.ui.scanner

import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.wb.go.app.PREFIX_QR_CODE
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerState
import java.util.concurrent.TimeUnit

class ScannerViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: ScannerInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _scannerAction = SingleLiveEvent<ScannerState>()
    val scannerAction: LiveData<ScannerState>
        get() = _scannerAction

    private var oldBarcode: String = ""
    private var erase: Disposable? = null

    init {
        addSubscription(interactor.observeScannerState().subscribe { _scannerAction.value = it })
    }

    fun onBarcodeScanned(barcode: String) {
        if (barcode.startsWith(PREFIX_QR_CODE)) {
            if (oldBarcode == barcode) {
                erase?.dispose()
                erase = Observable.timer(5, TimeUnit.SECONDS).subscribe { clearMemoryBarcode() }
            } else {
                _scannerAction.value = ScannerState.BeepScan
                interactor.barcodeScanned(barcode)
            }
            oldBarcode = barcode
        }
    }

    fun clearMemoryBarcode() {
        oldBarcode = ""
    }

}