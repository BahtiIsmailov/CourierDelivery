package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Observable
import io.reactivex.Single

interface DcUnloadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun observeScanProcess(): Observable<DcUnloadingData>

    fun findDcUnloadedHandleBoxes(): Single<List<DcReturnHandleBarcodeEntity>>

    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingBarcodeEntity>>

    fun observeDcUnloadedBoxes(): Observable<DcUnloadingScanBoxEntity>

    fun scannerAction(scannerAction: ScannerAction)

}