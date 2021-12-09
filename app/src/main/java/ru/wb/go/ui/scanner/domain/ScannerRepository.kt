package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ScannerRepository {

    fun scannerAction(barcode: String)

    fun observeBarcodeScanned(barcodeSubject: PublishSubject<String>): Observable<String>

    fun observeBarcodeScanned(): Observable<String>

    fun scannerState(state: ScannerState)

    fun observeScannerState(): Observable<ScannerState>

}