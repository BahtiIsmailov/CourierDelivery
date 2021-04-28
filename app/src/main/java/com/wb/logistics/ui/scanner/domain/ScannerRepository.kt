package com.wb.logistics.ui.scanner.domain

import io.reactivex.Observable

interface ScannerRepository {

    fun barcodeScanned(barcode: String)

    fun observeBarcodeScanned(): Observable<String>

    fun scannerAction(action: ScannerAction)

    fun observeScannerAction(): Observable<ScannerAction>

}