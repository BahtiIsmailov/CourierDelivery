package com.wb.logistics.network.headers

import android.os.ConditionVariable
import android.text.TextUtils
import com.wb.logistics.network.token.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean


class RefreshTokenInterceptor(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val headerManager: HeaderManager,
    private val tokenManager: TokenManager,
) : Interceptor {

    private val lock = ConditionVariable(true)
    private val mIsRefreshing: AtomicBoolean = AtomicBoolean(false)

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
                if (mIsRefreshing.compareAndSet(false, true)) {
                    lock.close()
                    refreshTokenRepository.refreshAccessTokens()
                    lock.open()
                    mIsRefreshing.set(false)
                } else {
                    val conditionOpened = lock.block(REFRESH_TIME_OUT)
                    if (conditionOpened) {
                        response = chain.proceed(newRequest)
                    }
                }
            }
        }
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            tokenManager.clear()
        }
        return response
    }

    companion object {
        const val REFRESH_TIME_OUT = 15000L
    }

}