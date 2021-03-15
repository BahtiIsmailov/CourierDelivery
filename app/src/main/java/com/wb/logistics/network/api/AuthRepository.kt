package com.wb.logistics.network.api

import com.wb.logistics.network.api.remote.AuthRemote
import com.wb.logistics.network.api.remote.CheckPhoneRemote
import io.reactivex.Single

interface AuthRepository {

    fun authByPhoneOrPassword(
        password: String,
        phone: String,
        useSMS: Boolean
    ): Single<AuthRemote>

    fun checkExistPhone(phone: String): Single<CheckPhoneRemote>

}