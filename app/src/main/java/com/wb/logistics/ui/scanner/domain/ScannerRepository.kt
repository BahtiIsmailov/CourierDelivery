package com.wb.logistics.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ScannerRepository {

    fun barcodeScanned(barcode: String)

    fun observeBarcodeScanned(barcodeSubject: PublishSubject<String>): Observable<String>

    fun scannerAction(action: ScannerAction)

    fun observeScannerAction(): Observable<ScannerAction>

}