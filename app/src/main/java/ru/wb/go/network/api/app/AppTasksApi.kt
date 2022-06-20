package ru.wb.go.network.api.app


import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.wb.go.network.api.app.remote.courier.CourierOrdersResponse
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse

interface AppTasksApi {

    @GET("{version}/free-tasks/offices")
    suspend fun freeTasksOffices(
        @Path(value = "version", encoded = true) version: String
    ):  CourierWarehousesResponse

    @GET("{version}/free-tasks")
    suspend fun freeTasks(
        @Path(value = "version", encoded = true) version: String,
        @Query("srcOfficeID") srcOfficeID: Int
    ):  CourierOrdersResponse

}