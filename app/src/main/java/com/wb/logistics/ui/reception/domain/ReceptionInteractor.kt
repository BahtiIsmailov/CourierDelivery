package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ReceptionInteractor {

    fun deleteScannedBoxes(checkedBoxes: List<String>): Completable

    fun boxScanned(barcode: String, isManualInput: Boolean)

    fun observeScanProcess(): Observable<ScanBoxData>

    fun observeScannedBoxes(): Observable<List<AttachedBoxEntity>>

    fun readBoxesScanned(): Single<List<AttachedBoxEntity>>

    fun sendAwaitBoxes(): Single<Int>

    fun addMockScannedBox(): Completable

    fun scannerAction(scannerAction: ScannerAction)

}