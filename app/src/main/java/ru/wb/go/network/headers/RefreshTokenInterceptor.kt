package ru.wb.go.network.headers

import android.os.ConditionVariable
import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.Response
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.token.TokenManager
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean


class RefreshTokenInterceptor(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val headerManager: HeaderManager,
    private val tokenManager: TokenManager,
) : Interceptor {

    private val lock = ConditionVariable(true)
    private val isRefreshing: AtomicBoolean = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val builder = request.newBuilder()

        for ((key, value) in headerManager.headerApiMap) {
            builder.addHeader(key, value)
        }
        val newRequest = builder.build()
        var response = chain.proceed(newRequest)

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (!TextUtils.isEmpty(tokenManager.bearerToken())) {
                if (isRefreshing.compareAndSet(false, true)) {
                    lock.close()
                    refreshTokenRepository.refreshAccessTokenSync()
                    lock.open()
                    isRefreshing.set(false)
                } else {
                    val conditionOpened = lock.block(REFRESH_TIME_OUT)
                    if (conditionOpened) {
                        response = chain.proceed(newRequest)
                    }
                }
            }
        }
        return response
    }

    companion object {
        const val REFRESH_TIME_OUT = 15000L
    }

}