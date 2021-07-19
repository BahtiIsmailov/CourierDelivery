package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun observeUnloadingProcess(currentOfficeId: Int): Observable<UnloadingData>

    fun scanLoaderProgress(): Observable<ScanProgressData>

    fun observeCountUnloadReturnedBoxAndSwitchScreen(currentOfficeId: Int): Observable<Int>

    fun scannerAction(scannerAction: ScannerAction)

    fun isUnloadingComplete(currentOfficeId: Int): Single<Boolean>

    fun officeNameById(currentOfficeId: Int): Single<String>

}