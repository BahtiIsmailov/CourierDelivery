package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import io.reactivex.Single

interface FlightDeliveriesInteractor {

    fun flightId(): Single<Int>

    fun getScannedBoxesGroupByAddress() : Single<List<ScannedBoxGroupByAddressEntity>>

}