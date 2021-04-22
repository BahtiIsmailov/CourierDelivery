package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.remote.boxdeletefromflight.BoxDeleteFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxesfromflight.BoxesRemote
import com.wb.logistics.network.api.app.remote.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.remote.flight.FlightRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxScannedRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusesStateRemote
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.matchingboxes.MatchingBoxesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface RemoteAppRepository {

    @GET("/api/v1/flight-statuses")
    fun flightStatuses(): Single<FlightStatusesRemote>

    @GET("/api/v1/flight")
    fun flight(): Single<FlightRemote?>

    @GET("/api/v1/flights/{flightID}/boxes")
    fun boxesFromFlight(@Path("flightID") flightID: String): Single<BoxesRemote>

    @GET("/api/v1/boxes/{barcode}")
    fun boxInfo(@Path("barcode") barcode: String): Single<BoxInfoRemote>

    @GET("/api/v1/flights/{flightID}/matching-boxes")
    fun matchingBoxes(@Path("flightID") flightID: String): Single<MatchingBoxesRemote>

    @POST("/api/v1/flights/{flightID}/boxes")
    fun flightBoxScannedToBalance(
        @Path("flightID") flightID: String,
        @Body box: FlightBoxScannedRemote,
    ): Completable

    @HTTP(method = "DELETE", path = "/api/v1/flights/{flightID}/boxes/{barcode}", hasBody = true)
    fun boxDeleteFromFlight(
        @Path("flightID") flightID: String,
        @Path("barcode") barcode: String,
        @Body box: BoxDeleteFromFlightRemote,
    ): Completable

    @GET("/api/v1/flight-statuses")
    fun getFlightStatus(): Single<StatusesStateRemote>

    @PUT("/api/v1/flights/{flightID}/status")
    fun putFlightStatus(
        @Path("flightID") flightID: String,
        @Body statusRemote: StatusRemote,
    ): Completable

    @GET("/api/v1/flights/{flightID}/status")
    fun getFlightsStatus(@Path("flightID") flightID: String, @Body statusesStateRemote: StatusesStateRemote)

    @GET("/api/v1/time")
    fun getTime(): Single<TimeRemote>

}