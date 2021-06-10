package com.wb.logistics.ui.scanner.domain

import com.wb.logistics.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ScannerRepositoryImpl : ScannerRepository {

    private val actionBarcodeScannedSubject = PublishSubject.create<String>()
    private val scannerActionSubject = PublishSubject.create<ScannerAction>()

    override fun barcodeScanned(barcode: String) {
        actionBarcodeScannedSubject.onNext(barcode)
    }

    override fun observeBarcodeScanned(): Observable<String> = actionBarcodeScannedSubject


    override fun scannerAction(action: ScannerAction) {
        LogUtils { logDebugApp(action.toString()) }
        scannerActionSubject.onNext(action)
    }

    override fun observeScannerAction(): Observable<ScannerAction> {
        return scannerActionSubject
    }

}