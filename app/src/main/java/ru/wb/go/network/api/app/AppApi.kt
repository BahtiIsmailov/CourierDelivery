package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.http.*
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.VersionAppResponse
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountsResponse
import ru.wb.go.network.api.app.remote.bank.BankResponse
import ru.wb.go.network.api.app.remote.billing.BillingCommonResponse
import ru.wb.go.network.api.app.remote.courier.*
import ru.wb.go.network.api.app.remote.payments.PaymentsRequest


interface AppApi {

    @POST("{version}/me/courier-documents")
    suspend fun saveCourierDocuments(
        @Path(value = "version", encoded = true) version: String,
        @Body courierDocuments: CourierDocumentsRequest,
    )

    @GET("{version}/me/courier-documents")
    suspend fun getCourierDocuments(
        @Path(value = "version", encoded = true) version: String,
    ):  CourierDocumentsResponse

    //==============================================================================================
    //tasks
    //==============================================================================================

    @GET("{version}/tasks/my")
    suspend fun tasksMy(
        @Path(value = "version", encoded = true) version: String,
    ):  MyTaskResponse

    @POST("{version}/tasks/{taskID}/courier")
    suspend fun reserveTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body courierAnchorResponse: CourierAnchorResponse
    )

    @HTTP(method = "DELETE", path = "{version}/tasks/{taskID}/courier", hasBody = true)
    suspend fun deleteTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    )

    @GET("{version}/tasks/{taskID}/boxes")
    suspend fun taskBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    ):  CourierTaskBoxesResponse

    @POST("{version}/tasks/{taskID}/statuses/start")
    suspend fun setStartTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ):  StartTaskResponse

    //TODO("Арсений")
    @POST("{version}/tasks/{taskID}/statuses/ready")
    suspend fun taskStatusesReady(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ):  TaskCostResponse

    @POST("{version}/tasks/{taskID}/statuses/intransit")
    suspend fun taskStatusesIntransit(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    )

    @POST("{version}/tasks/{taskID}/statuses/end")
    suspend fun taskStatusesEnd(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    )

    //==============================================================================================
    //billing
    //==============================================================================================

    @GET("{version}/billing/account")
    suspend fun getBilling(
        @Path(value = "version", encoded = true) version: String,
        @Query("showTransactions") isShowTransactions: Boolean
    ): BillingCommonResponse

    @POST("{version}/payments")
    suspend fun doTransaction(
        @Path(value = "version", encoded = true) version: String,
        @Body paymentsRequest: PaymentsRequest
    )

    @GET("{version}/banks")
    suspend fun getBank(
        @Path(value = "version", encoded = true) version: String,
        @Query("bic") bic: String
    ): BankResponse

    @GET("{version}/me/bank-accounts")
    suspend fun getBankAccounts(
        @Path(value = "version", encoded = true) version: String
    ):  AccountsResponse

    @PUT("{version}/me/bank-accounts")
    suspend fun setBankAccounts(
        @Path(value = "version", encoded = true) version: String,
        @Body accountRequest: List<AccountRequest>
    )

    @GET("{version}/settings/mobile-version")
    suspend fun getAppActualVersion(
        @Path(value = "version", encoded = true) version: String
    ):  VersionAppResponse

}