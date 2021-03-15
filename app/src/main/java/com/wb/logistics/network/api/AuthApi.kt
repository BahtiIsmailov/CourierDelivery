package com.wb.logistics.network.api

import com.wb.logistics.network.api.remote.AuthRemote
import com.wb.logistics.network.api.remote.CheckPhoneRemote
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("/api/v1/auth")
    fun authByPhoneOrPassword(
        @Query("password") password: String,
        @Query("phone") phone: String,
        @Query("useSMS") useSMS: Boolean
    ): Single<AuthRemote>


    @GET("/api/v1/auth/{phone}")
    fun checkExistPhone(
        @Path("phone") phone: String
    ): Single<CheckPhoneRemote>

    @GET("/api/v1/auth/{phone}")
    fun authByPhoneOrPasswordKtx(
        @Path("phone") phone: String
    ): Call<ResponseBody>

}