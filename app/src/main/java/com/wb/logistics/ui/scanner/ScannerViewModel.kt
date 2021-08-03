package com.wb.logistics.ui.scanner

import androidx.lifecycle.LiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class ScannerViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: ScannerInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _scannerAction = SingleLiveEvent<ScannerAction>()
    val scannerAction: LiveData<ScannerAction>
        get() = _scannerAction

    private var oldBarcode: String = ""
    private var erase: Disposable? = null

    init {
        addSubscription(interactor.observeScannerAction().subscribe { _scannerAction.value = it })
    }

    fun onBarcodeScanned(barcode: String) {
        if (barcode.startsWith("TRBX")) {
            if (oldBarcode == barcode) {
                erase?.dispose()
                erase = Observable.timer(5, TimeUnit.SECONDS).subscribe { clearMemoryBarcode() }
            } else {
                _scannerAction.value = ScannerAction.BeepScan
                interactor.barcodeScanned(barcode)
            }
            oldBarcode = barcode
        }
    }
    fun clearMemoryBarcode() {
        oldBarcode = ""
    }

}