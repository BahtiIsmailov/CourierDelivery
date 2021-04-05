package com.wb.logistics.db

import com.wb.logistics.db.entity.boxesfromflight.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface LocalRepository {

    fun saveFlight(
        flightEntity: FlightEntity, flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable

    fun readFlight(): Flowable<SuccessOrEmptyData<FlightData>>

    fun removeFlight()

    fun saveBoxesFromFlight(boxesEntity: List<FlightBoxEntity>): Completable

    fun readBoxesFromFlight(): Single<List<FlightBoxEntity>>

    fun removeBoxesFromFlight()

}