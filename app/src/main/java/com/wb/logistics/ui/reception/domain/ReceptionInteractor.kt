package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ReceptionInteractor {

    fun deleteScannedBoxes(checkedBoxes: List<String>): Completable

    fun boxScanned(barcode: String, isManualInput: Boolean)

    fun observeScanProcess(): Observable<ScanBoxData>

    fun observeScannedBoxes(): Observable<List<ScannedBoxEntity>>

    fun readBoxesScanned(): Single<List<ScannedBoxEntity>>

    fun sendAwaitBoxes(): Single<Int>

    fun addMockScannedBox(): Completable

}