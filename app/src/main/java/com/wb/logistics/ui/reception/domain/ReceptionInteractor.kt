package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface ReceptionInteractor {

    fun deleteScannedBoxes(checkedBoxes: List<String>): Completable

    fun boxScanned(barcode: String, isManualInput: Boolean)

    fun observeScanProcess(): Observable<ScanBoxData>

    fun observeScannedBoxes(): Observable<List<ScannedBoxEntity>>

    fun addMockScannedBox(): Completable

}