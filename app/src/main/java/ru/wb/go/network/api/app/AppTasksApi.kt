package ru.wb.go.network.api.app


import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.wb.go.network.api.app.remote.courier.*

interface AppTasksApi {

    @GET("{version}/free-tasks/offices")
    suspend fun freeTasksOffices(
        @Path(value = "version", encoded = true) version: String
    ):  CourierWarehousesResponse

    @GET("{version}/free-tasks")
    suspend fun freeTasks(
        @Path(value = "version", encoded = true) version: String,
        @Query("srcOfficeID") srcOfficeID: Int
    ):  List<CourierOrderResponse>

    @GET("{version}/free-tasks")
    suspend fun freeTaskForDemo(
        @Path(value = "version", encoded = true) version: String,
        @Query("srcOfficeID") srcOfficeID: Int
    ): CourierOrderResponseFromDemo

    @GET("/api/v1/routes/{rid}/boxes/count")
    suspend fun boxCountWithRidMask(
        @Path(value = "rid",encoded = true) rid: Long
    ):TaskBoxCountResponse
}