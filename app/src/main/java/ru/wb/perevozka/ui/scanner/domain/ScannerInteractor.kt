package ru.wb.perevozka.ui.scanner.domain

import io.reactivex.Observable

interface ScannerInteractor {

    fun barcodeScanned(barcode: String)

    fun observeScannerState(): Observable<ScannerState>

}