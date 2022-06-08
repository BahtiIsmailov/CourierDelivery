package ru.wb.go.network.api.app

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.wb.go.network.api.app.remote.courier.CourierOrdersResponse
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse

interface AppTasksApi {

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

}