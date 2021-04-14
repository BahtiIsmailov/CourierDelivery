package com.wb.logistics.network.headers

import com.wb.logistics.BuildConfig
import com.wb.logistics.network.token.TokenManager
import okhttp3.logging.HttpLoggingInterceptor

object InterceptorFactory {

    fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    fun createRefreshTokenInterceptor(
        refreshTokenRepository: RefreshTokenRepository,
        headerManager: HeaderManager,
        tokenManager: TokenManager,
    ): RefreshTokenInterceptor {
        return RefreshTokenInterceptor(refreshTokenRepository, headerManager, tokenManager)
    }

}