package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByAddressEntity
import io.reactivex.Single

interface FlightDeliveriesInteractor {

    fun flightId(): Single<Int>

    fun getAttachedBoxesGroupByAddress() : Single<List<AttachedBoxGroupByAddressEntity>>

}