package ru.wb.go.network.headers

import okhttp3.logging.HttpLoggingInterceptor
import ru.wb.go.BuildConfig
import ru.wb.go.network.token.TokenManager
import ru.wb.go.reader.MockResponse
import ru.wb.go.utils.analytics.YandexMetricManager

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

    fun createAppMetricResponseInterceptor(metric: YandexMetricManager): AppMetricResponseInterceptor {
        return AppMetricResponseInterceptor(metric)
    }

    fun createRefreshTokenInterceptor(
        refreshTokenRepository: RefreshTokenRepository,
        headerManager: HeaderManager,
        tokenManager: TokenManager,
    ): RefreshTokenInterceptor {
        return RefreshTokenInterceptor(refreshTokenRepository, headerManager, tokenManager)
    }

}