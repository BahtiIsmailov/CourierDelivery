package com.wb.logistics.network.api

import com.wb.logistics.network.api.remote.AuthRemote
import com.wb.logistics.network.api.remote.CheckPhoneRemote
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val rxSchedulerFactory: RxSchedulerFactory
) : AuthRepository {

    override fun authByPhoneOrPassword(
        password: String,
        phone: String,
        useSMS: Boolean
    ): Single<AuthRemote> {
        return authApi.authByPhoneOrPassword(password, phone, useSMS)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun checkExistPhone(phone: String): Single<CheckPhoneRemote> {
        return authApi.checkExistPhone(phone).compose(rxSchedulerFactory.applySingleSchedulers())
    }

}