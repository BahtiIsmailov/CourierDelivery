package ru.wb.go.network.api.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.api.auth.query.AuthBySmsOrPasswordQuery
import ru.wb.go.network.api.auth.response.StatisticsResponse
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.utils.managers.SettingsManager

class AuthRemoteRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val settingsManager: SettingsManager,
) : AuthRemoteRepository {

    override suspend fun auth(
        password: String, phone: String, useSMS: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            val requestBody = AuthBySmsOrPasswordQuery(phone, password, useSMS)
            val auth = authApi.auth(tokenManager.apiVersion(), requestBody)
            val tokenEntity = TokenEntity(auth.accessToken, auth.expiresIn, auth.refreshToken)
            saveToken(tokenEntity)
            turnOffDemo()
        }
    }

    override suspend fun couriersExistAndSavePhone(phone: String) {
        withContext(Dispatchers.IO) {
            try {
                authApi.couriersAuth(tokenManager.apiVersion(), phone)
                userManager.savePhone(phone)
            } catch (e: Exception) {
            }
        }
    }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    private fun turnOffDemo() {
        tokenManager.turnOffDemo()
    }

    override suspend fun statistics(): StatisticsResponse {
        return withContext(Dispatchers.IO){
            authApi.statistics(tokenManager.apiVersion())
        }
    }

    override suspend fun userInfo(): UserInfoEntity {
        return withContext(Dispatchers.IO){
            UserInfoEntity(tokenManager.userName(), tokenManager.userCompany())
        }
    }

    override fun clearCurrentUser() {
        tokenManager.clear()
        userManager.clearAll()
        settingsManager.resetSettings()
    }

    override fun userPhone(): String {
        return userManager.phone()
    }

}