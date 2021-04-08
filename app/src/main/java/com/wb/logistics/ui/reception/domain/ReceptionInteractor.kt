package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import io.reactivex.Observable

interface ReceptionInteractor {

    fun removeBoxes(checkedBoxes: List<Boolean>)

    fun boxScanned(barcode: String, isManualInput: Boolean)

    fun observeScanState(): Observable<ScanBoxData>

    fun observeFlightBoxes(): Observable<List<FlightBoxScannedEntity>>

}