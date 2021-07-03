package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightUnloadedAndUnloadCountEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable

    fun barcodeManualInput(barcode: String)

    fun observeScanProcess(currentOfficeId: Int): Observable<UnloadingData>

    fun observeCountUnloadReturnedBoxAndSwitchScreen(currentOfficeId: Int): Observable<Int>

    fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun observeUnloadedAndTakeOnFlightBoxes(currentOfficeId: Int): Observable<FlightUnloadedAndUnloadCountEntity>

    fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun observeReturnedAndMatchingBoxes(currentOfficeId: Int): Observable<Pair<List<FlightBoxEntity>, List<PvzMatchingBoxEntity>>>

    fun scannerAction(scannerAction: ScannerAction)

    fun completeUnloading(): Completable

    fun officeNameById(currentOfficeId: Int): Single<String>

}