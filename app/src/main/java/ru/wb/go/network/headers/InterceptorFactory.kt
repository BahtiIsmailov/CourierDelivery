package ru.wb.go.network.headers

import ru.wb.go.BuildConfig
import ru.wb.go.network.token.TokenManager
import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.reader.MockResponse

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