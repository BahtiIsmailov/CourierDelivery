package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.boxdeletefromflight.BoxDeleteFromFlightRemote
import com.wb.logistics.network.api.app.response.boxesfromflight.BoxesRemote
import com.wb.logistics.network.api.app.response.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.response.flight.FlightRemote
import com.wb.logistics.network.api.app.response.flightboxtobalance.FlightBoxScannedRemote
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
    fun flightBoxScannedToBalance(@Path("flightID") flightID: String, @Body box: FlightBoxScannedRemote): Completable

    @HTTP(method = "DELETE", path = "/api/v1/flights/{flightID}/boxes/{barcode}", hasBody = true)
    fun boxDeleteFromFlight(
        @Path("flightID") flightID: String,
        @Path("barcode") barcode: String,
        @Body box: BoxDeleteFromFlightRemote,
    ): Completable

}