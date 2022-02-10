package ru.wb.go.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.api.auth.response.StatisticsResponse

interface AuthRemoteRepository {

    fun auth(password: String, phone: String, useSMS: Boolean): Completable

    fun couriersExistAndSavePhone(phone: String): Completable

    fun statistics(): Single<StatisticsResponse>

    fun userInfo(): Single<UserInfoEntity>

    fun clearToken()

    fun userPhone(): String

}