package ru.wb.perevozka.network.headers

import ru.wb.perevozka.BuildConfig
import ru.wb.perevozka.network.token.TokenManager
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