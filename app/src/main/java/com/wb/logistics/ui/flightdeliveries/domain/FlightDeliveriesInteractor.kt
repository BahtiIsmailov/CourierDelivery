package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import io.reactivex.Completable
import io.reactivex.Single

interface FlightDeliveriesInteractor {

    fun flightId(): Single<Int>

    fun getAttachedBoxesGroupByOffice() : Single<List<AttachedBoxGroupByOfficeEntity>>

    fun getAttachedBoxes(): Single<Int>

    fun switchScreen(): Completable

    fun updatePvzAttachedBoxes(): Completable

}