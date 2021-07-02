package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcLoadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun removeScannedBoxes(checkedBoxes: List<String>): Completable

    fun observeScanProcess(): Observable<ScanProcessData>

    fun observeScannedBoxes(): Observable<List<FlightBoxEntity>>

    fun gate(): Single<String>

    fun switchScreen(): Completable

    fun scannerAction(scannerAction: ScannerAction)

}