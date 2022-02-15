package ru.wb.go.network.api.refreshtoken

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single

import ru.wb.go.app.AppConsts.REFRESH_TOKEN_INVALID
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.network.api.auth.query.RefreshTokenQuery
import ru.wb.go.network.api.auth.response.RefreshResponse
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.app.domain.AppNavRepository
import ru.wb.go.utils.analytics.YandexMetricManager


class RefreshTokenRepositoryImpl(
    private var server: RefreshTokenApi,
    private val tokenManager: TokenManager,
    private val metric: YandexMetricManager,
    private val appNavRepository: AppNavRepository
) : RefreshTokenRepository {

    override fun doRefreshToken(): RefreshResult {
        var retry = 0
        var result: RefreshResult

        do {
            result = refreshAccessTokenSync()
            retry++
        } while (result is RefreshResult.Failed && retry < RETRY)

        return result
    }

    private fun refreshAccessTokenSync(): RefreshResult {
        return try {
            val response = server.refreshAccessTokens(
                tokenManager.bearerToken(),
                RefreshTokenQuery(tokenManager.refreshToken())
            ).execute()
            if (response.isSuccessful) {
                val refreshResponse = response.body()
                if (refreshResponse != null) {
                    saveToken(convertTokenEntity(refreshResponse))
                    RefreshResult.Success
                } else RefreshResult.Failed
            } else {
                val gson = Gson()
                val type = object : TypeToken<ErrorResponse>() {}.type
                val errorResponse: ErrorResponse? =
                    gson.fromJson(response.errorBody()!!.charStream(), type)
                if (errorResponse?.error?.code == REFRESH_TOKEN_INVALID) {
                    tokenManager.clear()
                    RefreshResult.TokenInvalid
                } else {
                    metric.onTechErrorLog(
                        "RefreshToken",
                        "unknownResponse",
                        errorResponse?.toString() ?: "-"
                    )
                    RefreshResult.Failed
                }
            }
        } catch (ex: Exception) {
            metric.onTechErrorLog("RefreshToken", "catchException", ex?.message ?: "-")
            RefreshResult.Failed
        }

    }

    private fun convertTokenEntity(refreshResponse: RefreshResponse) =
        with(refreshResponse) {
            TokenEntity(accessToken, expiresIn, refreshToken)
        }

    private fun saveToken(tokenEntity: TokenEntity) {
        tokenManager.saveToken(tokenEntity)
    }

    override fun refreshAccessToken(): Completable {
        return Completable.fromSingle(
            Single.fromCallable {
                if (doRefreshToken() == RefreshResult.TokenInvalid) {
                    appNavRepository.navigate("to_auth")
                }
            }
        )
    }

    companion object {
        const val RETRY = 3
    }
}