package ru.wb.go.network.api.refreshtoken

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.wb.go.app.AppConsts.PHONE_IS_UNEXPECTED

import ru.wb.go.app.AppConsts.REFRESH_TOKEN_INVALID
import ru.wb.go.app.AppConsts.SERVICE_CODE_BAD_REQUEST
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.RefreshResponse
import ru.wb.go.network.exceptions.TimeoutException
import ru.wb.go.network.exceptions.UnknownException
import ru.wb.go.network.token.TokenManager
import ru.wb.go.utils.analytics.YandexMetricManager
import java.net.SocketTimeoutException

class RefreshTokenRepositoryImpl(
    private var server: RefreshTokenApi,
    private val tokenManager: TokenManager,
    private val metric: YandexMetricManager
) : RefreshTokenRepository {

    override suspend fun doRefreshToken(): RefreshResult {
        var retry = 0
        var result: RefreshResult

        do {
            result = refreshAccessTokenSync()
            retry++
        } while (result is RefreshResult.TimeOut && retry < RETRY)

        return result
    }

    private suspend fun refreshAccessTokenSync(): RefreshResult {
        return try {
            val response = server.refreshAccessTokens(
                tokenManager.bearerToken(),
                RefreshTokenQuery(tokenManager.refreshToken())
            )
            if (response.isSuccessful) {
                val refreshResponse = response.body()
                if (refreshResponse != null) {
                    saveToken(convertTokenEntity(refreshResponse))
                    RefreshResult.Success
                } else {
                    val ex = UnknownException("Empty body", "")
                    metric.onTechErrorLog("RefreshToken", "refreshSuccessResponse", "emptyBody")
                    RefreshResult.Failed(ex)
                }
            }
        else {
                val gson = Gson()
                val type = object : TypeToken<ErrorResponse>() {}.type
                val errorResponse: ErrorResponse? =
                    gson.fromJson(response.errorBody()!!.charStream(), type)
                val code = errorResponse?.error?.code
                if (code == REFRESH_TOKEN_INVALID ||
                    (code == PHONE_IS_UNEXPECTED || response.code() == SERVICE_CODE_BAD_REQUEST) //При удалении реквизитов курьера, телефона больше не сущестует
                ) {
                    tokenManager.clear()
                    RefreshResult.TokenInvalid
                } else {
                    val msg = errorResponse?.toString() ?: "-"
                    metric.onTechErrorLog("RefreshToken", "unknownResponse", msg)
                    val ex = UnknownException("Validation error", "")
                    RefreshResult.Failed(ex)
                }
            }
        } catch (ex: Exception) {
            metric.onTechErrorLog("RefreshToken", "catchException", ex.message ?: "-")
            if (ex is TimeoutException || ex is SocketTimeoutException) {
                RefreshResult.TimeOut
            } else
                RefreshResult.Failed(ex)
        }

    }

    private fun convertTokenEntity(refreshResponse: RefreshResponse) =
        with(refreshResponse) {
            TokenEntity(accessToken, expiresIn, refreshToken)
        }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    companion object {
        const val RETRY = 3
    }
}