package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*
import ru.wb.go.network.api.app.remote.CarNumberRequest
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.VersionAppResponse
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountsResponse
import ru.wb.go.network.api.app.remote.bank.BankResponse
import ru.wb.go.network.api.app.remote.billing.BillingCommonResponse
import ru.wb.go.network.api.app.remote.courier.*
import ru.wb.go.network.api.app.remote.payments.PaymentsRequest

interface AppDynamicApi {

    @POST("{version}/me/courier-documents")
    fun saveCourierDocuments(
        @Path(value = "version", encoded = true) version: String,
        @Body courierDocuments: CourierDocumentsRequest,
    ): Completable

    @GET("{version}/me/courier-documents")
    fun getCourierDocuments(
        @Path(value = "version", encoded = true) version: String,
    ): Single<CourierDocumentsResponse>

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
    ): Single<MyTaskResponse>

    @POST("{version}/tasks/{taskID}/courier")
    fun anchorTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body courierAnchorResponse: CourierAnchorResponse
    ): Completable

    @HTTP(method = "DELETE", path = "{version}/tasks/{taskID}/courier", hasBody = true)
    fun deleteTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    ): Completable

    @GET("{version}/tasks/{taskID}/boxes")
    fun taskBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    ): Single<CourierTaskBoxesResponse>

    @POST("{version}/tasks/{taskID}/statuses/start")
    fun setStartTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ): Single<StartTaskResponse>

    @POST("{version}/tasks/{taskID}/statuses/ready")
    fun taskStatusesReady(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ): Single<TaskCostResponse>

    @POST("{version}/tasks/{taskID}/statuses/intransit")
    fun taskStatusesIntransit(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ): Completable

    @POST("{version}/tasks/{taskID}/statuses/end")
    fun taskStatusesEnd(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
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
    fun getBilling(
        @Path(value = "version", encoded = true) version: String,
        @Query("showTransactions") isShowTransactions: Boolean
    ): Single<BillingCommonResponse>

    @POST("{version}/payments")
    fun doTransaction(
        @Path(value = "version", encoded = true) version: String,
        @Body paymentsRequest: PaymentsRequest
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
    fun getAppActualVersion(
        @Path(value = "version", encoded = true) version: String
    ): Single<VersionAppResponse>

    @GET
    fun getDynamicUrl(
        @Url url: String
    ): Single<ResponseBody>

}