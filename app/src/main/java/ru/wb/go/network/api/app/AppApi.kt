package ru.wb.go.network.api.app

import retrofit2.http.*
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.VersionAppResponse
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountsResponse
import ru.wb.go.network.api.app.remote.bank.BankResponse
import ru.wb.go.network.api.app.remote.billing.BillingCommonResponse
import ru.wb.go.network.api.app.remote.courier.*
import ru.wb.go.network.api.app.remote.payments.PaymentsRequest
import ru.wb.go.ui.courierunloading.data.FakeBeep


interface AppApi {

    @POST(" ")
    suspend fun saveCourierDocuments(
        @Path(value = "version", encoded = true) version: String,
        @Body courierDocuments: CourierDocumentsRequest,
    )

    @GET(" ")
    suspend fun getCourierDocuments(
        @Path(value = "version", encoded = true) version: String,
    ):  CourierDocumentsResponse

    //==============================================================================================
    //tasks
    //==============================================================================================

    @GET(" ")
    suspend fun tasksMy(
        @Path(value = "version", encoded = true) version: String,
    ): MyTaskResponse

    @POST(" ")
    suspend fun reserveTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body courierAnchorResponse: CourierAnchorResponse
    )

    //@HTTP(method = "DELETE", path = "{version}/tasks/{taskID}/courier", hasBody = true)
    @POST(" ")
    suspend fun deleteTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    )

    @GET(" ")
    suspend fun taskBoxes(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    ):  CourierTaskBoxesResponse

    @POST(" ")
    suspend fun setStartTask(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ):  StartTaskResponse


    @POST(" ")
    suspend fun sendBoxOnDatabaseEveryFiveMinutes(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Query("srcOfficeID") srcOfficeID: Int,
        @Body boxes: List<ApiBoxRequest>
    )

    @POST("{version}/{taskID}/ ")
    suspend fun taskStatusesReady(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Body boxes: List<ApiBoxRequest>
    ):  TaskCostResponse

    @POST(" ")
    suspend fun taskStatusesIntransit(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
        @Query("srcOfficeID") srcOfficeID: Int,
        @Body boxes: List<ApiBoxRequest>
    )

    @POST(" ")
    suspend fun taskStatusesEnd(
        @Path(value = "version", encoded = true) version: String,
        @Path("taskID") orderId: String,
    )

    //==============================================================================================
    //billing
    //==============================================================================================

    @GET(" ")
    suspend fun getBilling(
        @Path(value = "version", encoded = true) version: String,
        @Query("showTransactions") isShowTransactions: Boolean
    ): BillingCommonResponse

    @POST(" ")
    suspend fun doTransaction(
        @Path(value = "version", encoded = true) version: String,
        @Body paymentsRequest: PaymentsRequest
    )

    @GET(" ")
    suspend fun getBank(
        @Path(value = "version", encoded = true) version: String,
        @Query("bic") bic: String
    ): BankResponse

    @GET(" ")
    suspend fun getBankAccounts(
        @Path(value = "version", encoded = true) version: String
    ): AccountsResponse

    @POST(" ")
    suspend fun setBankAccounts(
        @Path(value = "version", encoded = true) version: String,
        @Body accountRequest: List<AccountRequest>
    )

    @GET(" ")
    suspend fun getAppActualVersion(
        @Path(value = "version", encoded = true) version: String
    ):  VersionAppResponse

}