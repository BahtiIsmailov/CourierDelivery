package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.remote.PutBoxFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxdeletefromflight.BoxDeleteFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.remote.flight.FlightRemote
import com.wb.logistics.network.api.app.remote.flightboxes.FlightBoxesRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxScannedRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusesStateRemote
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.matchingboxes.MatchingBoxesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.http.*

interface AppApi {

    @GET("{version}/flight")
    fun flight(
        @Path(value = "version", encoded = true) version: String,
    ): Single<FlightRemote?>

    @GET("{version}/flights/{flightID}/matching-boxes")
    fun matchingBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<MatchingBoxesRemote>

    @GET("{version}/boxes/{barcode}")
    fun boxInfo(
        @Path(value = "version", encoded = true) version: String,
        @Path("barcode") barcode: String,
    ): Maybe<BoxInfoRemote?>

    @GET("{version}/flights/{flightID}/boxes")
    fun flightBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<FlightBoxesRemote>

    @POST("{version}/flights/{flightID}/warehouse/boxes")
    fun warehouseBoxToBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: FlightBoxScannedRemote,
    ): Completable

    @POST("{version}/flights/{flightID}/pvz/boxes")
    fun pvzBoxToBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: FlightBoxScannedRemote,
    ): Completable

    @HTTP(method = "DELETE", path = "{version}/flights/{flightID}/boxes/{barcode}", hasBody = true)
    fun deleteBoxFromFlight(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Path("barcode") barcode: String,
        @Body box: BoxDeleteFromFlightRemote,
    ): Completable

    @PUT("{version}/flight-statuses")
    fun getFlightStatus(
        @Path(value = "version", encoded = true) version: String,
    ): Single<StatusesStateRemote>

    @PUT("{version}/flights/{flightID}/boxes/{barcode}")
    fun removeFromBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Path("barcode") barcode: String,
        @Body box: PutBoxFromFlightRemote,
    ): Completable

    @GET("{version}/flight-statuses")
    fun flightStatuses(
        @Path(value = "version", encoded = true) version: String,
    ): Single<FlightStatusesRemote>

    @PUT("{version}/flights/{flightID}/status")
    fun putFlightStatus(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body statusRemote: StatusRemote,
    ): Completable

    @GET("{version}/flights/{flightID}/status")
    fun getFlightStatus(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<StatusStateRemote>

    @GET("{version}/time")
    fun getTime(@Path(value = "version", encoded = true) version: String): Single<TimeRemote>

}