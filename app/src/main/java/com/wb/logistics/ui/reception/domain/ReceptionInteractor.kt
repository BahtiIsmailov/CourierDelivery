package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface ReceptionInteractor {

    fun deleteFlightBoxes(checkedBoxes: List<String>): Completable

    fun boxScanned(barcode: String, isManualInput: Boolean)

    fun observeScanState(): Observable<ScanBoxData>

    fun observeFlightBoxes(): Observable<List<FlightBoxScannedEntity>>

}