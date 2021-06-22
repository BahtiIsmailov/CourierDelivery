package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun removeReturnBoxes(dstOfficeId: Int, checkedBoxes: List<String>): Completable

    fun barcodeManualInput(barcode: String)

    fun observeScanProcess(dstOfficeId: Int): Observable<UnloadingData>

    fun observeCountUnloadReturnedBox(dstOfficeId: Int): Observable<Int>

    fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>>

    fun observeUnloadedBoxes(dstOfficeId: Int): Observable<List<UnloadedBoxEntity>>

    fun observeUnloadedAndAttachedBoxes(dstOfficeId: Int): Observable<Pair<List<UnloadedBoxEntity>, List<AttachedBoxEntity>>>

    fun observeReturnBoxes(dstOfficeId: Int): Observable<List<ReturnBoxEntity>>

    fun scannerAction(scannerAction: ScannerAction)

    fun completeUnloading(dstOfficeId: Int): Completable

    fun officeNameById(dstOfficeId: Int): Single<String>

}