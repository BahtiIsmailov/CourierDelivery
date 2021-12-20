package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.http.*
import ru.wb.go.network.api.app.remote.CarNumberRequest
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.VersionAppResponse
import ru.wb.go.network.api.app.remote.billing.BillingCommonResponse
import ru.wb.go.network.api.app.remote.courier.*

interface AppApi {

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
    ): Single<CourierTasksMyResponse>

    @POST("{version}/tasks/{taskID}/courier")
    fun anchorTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
        @Body courierAnchorResponse: CourierAnchorResponse
    ): Completable

    @HTTP(method = "DELETE", path = "{version}/tasks/{taskID}/courier", hasBody = true)
    fun deleteTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
    ): Completable

    @GET("{version}/task-statuses")
    fun taskStatuses(
        @Path(value = "version", encoded = true) version: String,
    ): Single<CourierTaskStatusesResponse>

    @GET("{version}/tasks/{taskID}/boxes")
    fun taskBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
    ): Single<CourierTaskBoxesResponse>

    @POST("{version}/tasks/{taskID}/statuses/start")
    fun taskStart(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
        @Body courierTaskStartRequest: List<CourierTaskStartRequest>
    ): Completable

    @POST("{version}/tasks/{taskID}/statuses/ready")
    fun taskStatusesReady(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
        @Body courierTaskStatusesIntransitRequest: List<CourierTaskStatusesIntransitRequest>
    ): Single<CourierTaskStatusesIntransitResponse>

    @POST("{version}/tasks/{taskID}/statuses/intransit")
    fun taskStatusesIntransit(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") flightID: String,
        @Body courierTaskStatusesIntransitRequest: List<CourierTaskStatusesIntransitRequest>
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

    //==============================================================================================
    //billing
    //==============================================================================================

    @GET("{version}/billing/account")
    fun billing(
        @Path(value = "version", encoded = true) version: String,
        @Query("showTransactions") isShowTransactions: Boolean
    ): Single<BillingCommonResponse>

    @POST("{version}/payments")
    fun payments(
        @Path(value = "version", encoded = true) version: String,
        @Body paymentRequest: PaymentRequest
    ): Completable

    @GET("{version}/banks")
    fun getBank(
        @Path(value = "version", encoded = true) version: String,
        @Query("bic") bic: String
    ): Maybe<BankResponse>

    @GET("{version}/me/bank-accounts")
    fun getBankAccounts(
        @Path(value = "version", encoded = true) version: String
    ): Single<AccountsResponse>

    @PUT("{version}/me/bank-accounts")
    fun setBankAccounts(
        @Path(value = "version", encoded = true) version: String,
        @Body accountRequest: List<AccountRequest>
    ): Completable

    @GET("{version}/settings/mobile-version")
    fun version(
        @Path(value = "version", encoded = true) version: String
    ): Single<VersionAppResponse>

}