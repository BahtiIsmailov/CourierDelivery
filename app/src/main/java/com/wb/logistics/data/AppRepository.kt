package com.wb.logistics.data

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.api.app.response.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.response.flightstatuses.FlightStatusesRemote
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface AppRepository {

    fun flightStatuses(): Single<FlightStatusesRemote>

    fun updateFlightAndBox(): Completable

    fun readFlight(): Flowable<SuccessOrEmptyData<FlightData>>

    fun boxInfo(barcode: String): Single<BoxInfoRemote>

    fun boxToFlight(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ): Completable

}