package com.wb.logistics.ui.auth.data

import com.wb.logistics.network.api.AuthApi
import com.wb.logistics.network.api.remote.AuthRemote
import com.wb.logistics.network.api.remote.CheckPhoneRemote
import io.reactivex.Single

class AuthRepositoryImpl(private val authApi: AuthApi) : AuthRepository {

    override fun authByPhoneOrPassword(
        password: String,
        phone: String,
        useSMS: Boolean
    ): Single<AuthRemote> {
        return authApi.authByPhoneOrPassword(password, phone, useSMS)
    }

    override fun checkPhone(phone: String): Single<CheckPhoneRemote> {
        return authApi.checkPhone(phone)
    }

}