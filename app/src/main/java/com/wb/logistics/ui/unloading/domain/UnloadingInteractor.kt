package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun deleteScannedBoxes(checkedBoxes: List<String>): Completable

    fun barcodeManualInput(barcode: String)

    fun observeScanProcess(dstOfficeId: Int): Observable<UnloadingData>

    fun observeAttachedBoxesByDstOfficeId(dstOfficeId: Int): Observable<List<AttachedBoxEntity>>

    fun observeUnloadedBoxes(dstOfficeId: Int): Observable<Pair<List<UnloadedBoxEntity>, List<AttachedBoxEntity>>>

    fun observeReturnBoxes(dstOfficeId: Int): Observable<List<ReturnBoxEntity>>

    fun readBoxesScanned(): Single<List<AttachedBoxEntity>>

    fun sendAwaitBoxes(): Single<Int>

    fun scannerAction(scannerAction: ScannerAction)

}