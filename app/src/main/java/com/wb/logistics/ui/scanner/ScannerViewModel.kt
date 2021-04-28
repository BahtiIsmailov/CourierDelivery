package com.wb.logistics.ui.scanner

import androidx.lifecycle.LiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerInteractor
import io.reactivex.disposables.CompositeDisposable

class ScannerViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: ScannerResourceProvider,
    private val interactor: ScannerInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _scannerAction = SingleLiveEvent<ScannerAction>()
    val scannerAction: LiveData<ScannerAction>
        get() = _scannerAction

    init {
        addSubscription(interactor.observeScannerAction().subscribe { _scannerAction.value = it })
    }

    fun onBarcodeScanned(barcode: String) {
        interactor.barcodeScanned(resourceProvider.getBarCodeBox(barcode))
    }

}