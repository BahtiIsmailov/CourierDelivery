package ru.wb.perevozka.network.api.app

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import ru.wb.perevozka.network.api.app.remote.CarNumberRequest
import ru.wb.perevozka.network.api.app.remote.CourierDocumentsRequest
import ru.wb.perevozka.network.api.app.remote.boxinfo.BoxInfoResponse
import ru.wb.perevozka.network.api.app.remote.courier.*
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


    @POST("{version}/me/courier-documents")
    fun courierDocuments(
        @Path(value = "version", encoded = true) version: String,
        @Body courierDocuments: CourierDocumentsRequest,
    ): Completable

    //==============================================================================================
    //tasks
    //==============================================================================================
    @GET("{version}/free-tasks/offices")
    fun freeTasksOffices(
        @Path(value = "version", encoded = true) version: String
    ): Single<CourierWarehousesResponse>

    @GET("{version}/free-tasks")
    fun freeTasks(
        @Path(value = "version", encoded = true) version: String,
        @Query("srcOfficeID") srcOfficeID: Int
    ): Single<CourierOrdersResponse>

    @GET("{version}/tasks/my")
    fun tasksMy(
        @Path(value = "version", encoded = true) version: String,
    ): Single<CourierTasksMy>

    @POST("{version}/tasks/{taskID}/courier")
    fun anchorTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
    ): Single<CourierAnchorResponse>

    @HTTP(method = "DELETE", path = "{version}tasks/{taskID}/courier", hasBody = true)
    fun deleteTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
    ): Completable

    @GET("{version}/task-statuses")
    fun taskStatuses(
        @Path(value = "version", encoded = true) version: String,
    ): Single<CourierTaskStatusesResponse>

    @POST("{version}/tasks/{taskID}/statuses/start")
    fun taskStart(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
        @Body courierTaskStartRequest: CourierTaskStartRequest
    ): Completable

    @POST("{version}/tasks/{taskID}/statuses/intransit")
    fun taskStatusesIntransit(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
        @Body courierTaskStatusesIntransitRequest: CourierTaskStatusesIntransitRequest
    ): Completable

    @POST("{version}/tasks/{taskID}/statuses/end")
    fun taskStatusesEnd(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
    ): Completable

    @PUT("{version}/couriers/me/cars")
    fun putCarNumbers(
        @Path(value = "version", encoded = true) version: String,
        @Body carNumbersRequest: List<CarNumberRequest>
    ): Completable

}