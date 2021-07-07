package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable

    fun barcodeManualInput(barcode: String)

    fun observeUnloadingProcess(currentOfficeId: Int): Observable<UnloadingData>

    fun observeCountUnloadReturnedBoxAndSwitchScreen(currentOfficeId: Int): Observable<Int>

    fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun scannerAction(scannerAction: ScannerAction)

    fun isUnloadingComplete(currentOfficeId: Int): Single<Boolean>

    fun officeNameById(currentOfficeId: Int): Single<String>

}