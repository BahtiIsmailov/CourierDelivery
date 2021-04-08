package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.boxdeletefromflight.BoxDeletFromFlightRemote
import com.wb.logistics.network.api.app.response.boxesfromflight.BoxesRemote
import com.wb.logistics.network.api.app.response.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.response.boxtoflight.BoxToFlightRemote
import com.wb.logistics.network.api.app.response.flight.FlightRemote
import com.wb.logistics.network.api.app.response.flightstatuses.FlightStatusesRemote
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface RemoteRepository {

    @GET("/api/v1/flight-statuses")
    fun flightStatuses(): Single<FlightStatusesRemote>

    @GET("/api/v1/flight")
    fun flight(): Single<FlightRemote?>

    @GET("/api/v1/flights/{flightID}/boxes")
    fun boxesFromFlight(@Path("flightID") flightID: String): Single<BoxesRemote>

    @GET("/api/v1/boxes/{barcode}")
    fun boxInfo(@Path("barcode") barcode: String): Single<BoxInfoRemote>

    @POST("/api/v1/flights/{flightID}/boxes")
    fun boxToFlight(@Path("flightID") flightID: String, @Body box: BoxToFlightRemote): Completable

    @DELETE("/api/v1/flights/{flightID}/boxes/{barcode}")
    fun boxDeleteFromFlight(
        @Path("flightID") flightID: String,
        @Path("barcode") barcode: String,
        @Body box: BoxDeletFromFlightRemote,
    ): Completable

}