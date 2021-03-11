package com.wb.logistics.network.api

import com.wb.logistics.network.api.remote.AuthRemote
import com.wb.logistics.network.api.remote.CheckPhoneRemote
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("auth")
    fun authByPhoneOrPassword(
        @Query("password") password: String,
        @Query("phone") phone: String,
        @Query("useSMS") useSMS: Boolean
    ): Single<AuthRemote>


    @GET("auth/{phone}")
    fun checkPhone(
        @Path("phone") phone: String
    ): Single<CheckPhoneRemote>

}