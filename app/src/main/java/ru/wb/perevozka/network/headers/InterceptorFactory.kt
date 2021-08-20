package ru.wb.perevozka.network.headers

import ru.wb.perevozka.BuildConfig
import ru.wb.perevozka.network.token.TokenManager
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.perevozka.reader.MockResponse

object InterceptorFactory {

    fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    fun createAuthMockResponseInterceptor(apiServer: String): AuthMockResponseInterceptor {
        return AuthMockResponseInterceptor(apiServer)
    }

    fun createAppMockResponseInterceptor(apiServer: String, mockResponse : MockResponse): AppMockResponseInterceptor {
        return AppMockResponseInterceptor(apiServer, mockResponse)
    }

    fun createRefreshTokenInterceptor(
        refreshTokenRepository: RefreshTokenRepository,
        headerManager: HeaderManager,
        tokenManager: TokenManager,
    ): RefreshTokenInterceptor {
        return RefreshTokenInterceptor(refreshTokenRepository, headerManager, tokenManager)
    }

}