package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable

    fun barcodeManualInput(barcode: String)

    fun observeScanProcess(currentOfficeId: Int): Observable<UnloadingData>

    fun observeCountUnloadReturnedBox(currentOfficeId: Int): Observable<Int>

    fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<AttachedBoxEntity>>

    fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun observeUnloadedAndAttachedBoxes(currentOfficeId: Int): Observable<Pair<List<FlightBoxEntity>, List<AttachedBoxEntity>>>

    fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun scannerAction(scannerAction: ScannerAction)

    fun completeUnloading(): Completable

    fun officeNameById(currentOfficeId: Int): Single<String>

}