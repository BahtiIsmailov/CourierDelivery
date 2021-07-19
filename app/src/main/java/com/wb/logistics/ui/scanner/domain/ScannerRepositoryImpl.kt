package com.wb.logistics.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ScannerRepositoryImpl : ScannerRepository {

    private lateinit var actionBarcodeScannedSubject: PublishSubject<String>

    private val scannerActionSubject = PublishSubject.create<ScannerAction>()

    override fun barcodeScanned(barcode: String) {
        actionBarcodeScannedSubject.onNext(barcode)
    }

    override fun observeBarcodeScanned(barcodeSubject: PublishSubject<String>): Observable<String> {
        actionBarcodeScannedSubject= barcodeSubject
        return barcodeSubject
    }

    override fun scannerAction(action: ScannerAction) {
        scannerActionSubject.onNext(action)
    }

    override fun observeScannerAction(): Observable<ScannerAction> {
        return scannerActionSubject
    }

}