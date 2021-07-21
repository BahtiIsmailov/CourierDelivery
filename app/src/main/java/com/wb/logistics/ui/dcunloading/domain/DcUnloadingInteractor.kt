package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.ui.dcloading.domain.ScanProgressData
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcUnloadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun observeUnloadingProcess(): Observable<DcUnloadingData>

    fun scanLoaderProgress():  Observable<ScanProgressData>

    fun findDcUnloadedHandleBoxes(): Single<List<DcReturnHandleBarcodeEntity>>

    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingBarcodeEntity>>

    fun isBoxesUnloaded(): Single<Boolean>

    fun scannerAction(scannerAction: ScannerAction)

    fun switchScreenToClosed(): Completable

}