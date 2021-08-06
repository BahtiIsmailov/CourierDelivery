package ru.wb.perevozka.network.api.app

import ru.wb.perevozka.network.api.app.remote.boxinfo.BoxInfoResponse
import ru.wb.perevozka.network.api.app.remote.deleteboxesfromflight.RemoveBoxesFromFlightRequest
import ru.wb.perevozka.network.api.app.remote.flight.FlightResponse
import ru.wb.perevozka.network.api.app.remote.flightboxes.FlightBoxesResponse
import ru.wb.perevozka.network.api.app.remote.flightboxtobalance.FlightBoxToBalanceRequest
import ru.wb.perevozka.network.api.app.remote.flightlog.FlightLogRequest
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusResponse
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusStateResponse
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusesStateResponse
import ru.wb.perevozka.network.api.app.remote.flightstatuses.FlightStatusesResponse
import ru.wb.perevozka.network.api.app.remote.pvz.BoxFromPvzBalanceRequest
import ru.wb.perevozka.network.api.app.remote.pvz.BoxFromPvzBalanceResponse
import ru.wb.perevozka.network.api.app.remote.pvzmatchingboxes.PvzMatchingBoxesResponse
import ru.wb.perevozka.network.api.app.remote.time.TimeResponse
import ru.wb.perevozka.network.api.app.remote.tracker.BoxTrackerRequest
import ru.wb.perevozka.network.api.app.remote.warehouse.BoxFromWarehouseBalanceRequest
import ru.wb.perevozka.network.api.app.remote.warehouse.BoxFromWarehouseBalanceResponse
import ru.wb.perevozka.network.api.app.remote.warehouse.BoxToWarehouseBalanceRequest
import ru.wb.perevozka.network.api.app.remote.warehouse.BoxToWarehouseBalanceResponse
import ru.wb.perevozka.network.api.app.remote.warehousematchingboxes.WarehouseMatchingBoxesResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface AppApi {

    //==============================================================================================
    //flight
    //==============================================================================================

    @GET("{version}/flight")
    fun flight(
        @Path(value = "version", encoded = true) version: String,
    ): Single<FlightResponse>

    @POST("{version}/flights-logs")
    fun flightsLogs(
        @Path(value = "version", encoded = true) version: String,
        @Body flightsLogsRequest: List<FlightLogRequest>
    ): Completable

    //==============================================================================================
    //boxes
    //==============================================================================================

    @GET("{version}/flights/{flightID}/warehouse/matching-boxes")
    fun warehouseMatchingBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<WarehouseMatchingBoxesResponse>

    @GET("{version}/flights/{flightID}/pvz/matching-boxes")
    fun pvzMatchingBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<PvzMatchingBoxesResponse>

    @GET("{version}/boxes/{barcode}")
    fun boxInfo(
        @Path(value = "version", encoded = true) version: String,
        @Path("barcode") barcode: String,
    ): Single<BoxInfoResponse?>

    @GET("{version}/flights/{flightID}/boxes")
    fun flightBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<FlightBoxesResponse>

    @HTTP(method = "DELETE", path = "{version}/flights/{flightID}/boxes", hasBody = true)
    fun removeBoxesFromFlight(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: RemoveBoxesFromFlightRequest,
    ): Completable

    //==============================================================================================
    //boxes pvz
    //==============================================================================================
    @POST("{version}/flights/{flightID}/pvz/scan")
    fun putBoxToPvzBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: FlightBoxToBalanceRequest,
    ): Completable

    @PUT("{version}/flights/{flightID}/pvz/scan")
    fun removeBoxFromPvzBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: BoxFromPvzBalanceRequest,
    ): Single<BoxFromPvzBalanceResponse>

    //==============================================================================================
    //boxes warehouse
    //==============================================================================================
    @POST("{version}/flights/{flightID}/warehouse/scan")
    fun putBoxToWarehouseBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: BoxToWarehouseBalanceRequest,
    ): Single<BoxToWarehouseBalanceResponse>

    @PUT("{version}/flights/{flightID}/warehouse/scan")
    fun removeBoxFromWarehouseBalance(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body box: BoxFromWarehouseBalanceRequest,
    ): Single<BoxFromWarehouseBalanceResponse>

    //==============================================================================================
    //flight status
    //==============================================================================================
    @PUT("{version}/flight-statuses")
    fun getFlightStatus(
        @Path(value = "version", encoded = true) version: String,
    ): Single<StatusesStateResponse>

    @GET("{version}/flight-statuses")
    fun flightStatuses(
        @Path(value = "version", encoded = true) version: String,
    ): Single<FlightStatusesResponse>

    @PUT("{version}/flights/{flightID}/status")
    fun putFlightStatus(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
        @Body statusRemote: StatusResponse,
    ): Completable

    @GET("{version}/flights/{flightID}/status")
    fun getFlightStatus(
        @Path(value = "version", encoded = true) version: String,
        @Path("flightID") flightID: String,
    ): Single<StatusStateResponse>

    //==============================================================================================
    //time
    //==============================================================================================
    @GET("{version}/time")
    fun getTime(@Path(value = "version", encoded = true) version: String): Single<TimeResponse>

    //==============================================================================================
    //boxtracker
    //==============================================================================================
    @POST("{version}/scan-log")
    fun boxTracker(
        @Path(value = "version", encoded = true) version: String,
        @Body box: BoxTrackerRequest,
    ): Completable


}