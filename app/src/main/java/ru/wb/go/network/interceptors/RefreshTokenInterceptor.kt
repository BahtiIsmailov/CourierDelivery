package ru.wb.go.network.interceptors

import android.os.ConditionVariable
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import ru.wb.go.network.api.refreshtoken.RefreshResult
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.headers.HeaderManager
import ru.wb.go.network.headers.HeaderManager.Companion.TOKEN_AUTH
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.app.domain.AppNavRepository
import ru.wb.go.ui.app.domain.AppNavRepositoryImpl.Companion.INVALID_TOKEN
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean


class RefreshTokenInterceptor(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val headerManager: HeaderManager,
    private val tokenManager: TokenManager,
    private val appNavRepository: AppNavRepository
) : Interceptor {

    private val lock = ConditionVariable(true)
    private val isRefreshing: AtomicBoolean = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val builder = request.newBuilder()
        for ((key, value) in headerManager.headerApiMap) {
            builder.addHeader(key, value)
        }

        var response = chain.proceed(builder.build())

        if (tokenManager.bearerToken().isEmpty() || tokenManager.refreshToken().isEmpty()) {
            return response
        }

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (isRefreshing.compareAndSet(false, true)) {
                try {
                    lock.close()
                    when (refreshTokenRepository.doRefreshToken()) {
                        RefreshResult.TokenInvalid -> {
                            appNavRepository.navigate(INVALID_TOKEN)
                        }
                        RefreshResult.Success -> {
                            val newRequest = createRequest(request, tokenManager.bearerToken())
                            response.close()
                            response = chain.proceed(newRequest)
                        }
                        is RefreshResult.Failed -> {
                            // refresh failed. Attempt to continue without auth.
                            // if 500 or timeout - show error
                            response.close()
                            val newRequest = createRequest(request, "")
                            response = chain.proceed(newRequest)
                        }
                        RefreshResult.TimeOut -> {
                            val newRequest = createRequest(request, tokenManager.bearerToken())
                            response.close()
                            response = chain.proceed(newRequest)
                        }
                    }

                }finally {
                    lock.open()
                    isRefreshing.set(false)
                }
            } else {
                val conditionOpened = lock.block(REFRESH_TIME_OUT)
                if (conditionOpened ) {
                    val token = tokenManager.bearerToken()
                    if(token.isNotEmpty()) {
                        val newRequest = createRequest(request, token)
                        response = chain.proceed(newRequest)
                    }
                }
            }

        }
        return response
    }

    private fun createRequest(original: Request, token: String): Request {
        val builder = original.newBuilder()

        for ((key, value) in headerManager.headerApiMap) {
            if (key == TOKEN_AUTH) {
                builder.addHeader(key, token)
            } else
                builder.addHeader(key, value)
        }
        return builder.build()
    }

    companion object {
        const val REFRESH_TIME_OUT = 0L
    }

}