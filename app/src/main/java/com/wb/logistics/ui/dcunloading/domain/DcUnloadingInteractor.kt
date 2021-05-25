package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingHandleBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingListBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Observable
import io.reactivex.Single

interface DcUnloadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun observeScanProcess(): Observable<DcUnloadingData>

    fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>>

    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>>

    fun observeDcUnloadedBoxes(): Observable<DcUnloadingScanBoxEntity>

    fun scannerAction(scannerAction: ScannerAction)

}