package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcLoadingInteractor {

    fun boxScanned(barcode: String, isManualInput: Boolean)

    fun deleteScannedBoxes(checkedBoxes: List<String>): Completable

    fun observeScanProcess(): Observable<ScanProcessData>

    fun observeScannedBoxes(): Observable<List<AttachedBoxEntity>>

    fun sendAwaitBoxesCount(): Single<Int>

    fun switchScreen(): Completable

    fun scannerAction(scannerAction: ScannerAction)

}