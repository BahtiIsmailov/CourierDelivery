package ru.wb.perevozka.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ScannerRepository {

    fun barcodeScanned(barcode: String)

    fun observeBarcodeScanned(barcodeSubject: PublishSubject<String>): Observable<String>

    fun observeBarcodeScanned(): Observable<String>

    fun scannerAction(action: ScannerAction)

    fun observeScannerAction(): Observable<ScannerAction>

}