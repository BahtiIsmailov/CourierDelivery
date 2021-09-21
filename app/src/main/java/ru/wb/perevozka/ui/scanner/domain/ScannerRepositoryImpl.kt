package ru.wb.perevozka.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ScannerRepositoryImpl : ScannerRepository {

    private var scannerActionSubject = PublishSubject.create<String>()

    private val scannerStateSubject = PublishSubject.create<ScannerState>()

    override fun scannerAction(barcode: String) {
        scannerActionSubject.onNext(barcode)
    }

    override fun observeBarcodeScanned(barcodeSubject: PublishSubject<String>): Observable<String> {
        scannerActionSubject = barcodeSubject
        return barcodeSubject
    }

    override fun observeBarcodeScanned(): Observable<String> {
        return scannerActionSubject
    }

    override fun scannerState(state: ScannerState) {
        scannerStateSubject.onNext(state)
    }

    override fun observeScannerState(): Observable<ScannerState> {
        return scannerStateSubject
    }

}