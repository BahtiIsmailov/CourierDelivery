package ru.wb.go.network.api.auth

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.api.auth.response.StatisticsResponse

interface AuthRemoteRepository {

    suspend fun auth(password: String, phone: String, useSMS: Boolean)

    suspend fun couriersExistAndSavePhone(phone: String)

    suspend fun statistics(): StatisticsResponse

    suspend fun userInfo():  UserInfoEntity

    fun clearCurrentUser()

    fun userPhone(): String

}