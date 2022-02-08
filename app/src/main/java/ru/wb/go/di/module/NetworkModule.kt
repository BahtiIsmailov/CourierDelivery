package ru.wb.go.di.module

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.converter.gson.GsonConverterFactory
import ru.wb.go.network.NullOnEmptyConverterFactory
import ru.wb.go.network.certificate.CertificateStore
import ru.wb.go.network.certificate.CertificateStoreFactory
import ru.wb.go.network.client.OkHttpFactory
import ru.wb.go.network.exceptions.ErrorResolutionResourceProvider
import ru.wb.go.network.exceptions.ErrorResolutionResourceProviderImpl
import ru.wb.go.network.exceptions.ErrorResolutionStrategy
import ru.wb.go.network.exceptions.ErrorResolutionStrategyImpl
import ru.wb.go.network.headers.*
import ru.wb.go.network.rest.RefreshTokenRetrofitFactory
import ru.wb.go.network.rest.RetrofitFactory
import ru.wb.go.network.rx.RxHandlingCallAdapterFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.TokenManagerImpl
import ru.wb.go.network.token.UserManager
import ru.wb.go.network.token.UserManagerImpl
import ru.wb.go.reader.MockResponse
import ru.wb.go.reader.MockResponseImpl
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ConfigManager
import ru.wb.go.utils.prefs.SharedWorker
import java.net.URI

const val AUTH_NAMED_BASE_URL = "auth_named_base_url"
const val APP_NAMED_BASE_URL = "app_named_base_url"
const val AUTH_NAMED_HEADER_MANAGER = "auth_named_header_manager"
const val REFRESH_TOKEN_NAMED_HEADER_MANAGER = "refresh_token_named_header_manager"
const val APP_NAMED_HEADER_MANAGER = "app_named_header_manager"
const val APP_NAMED_DEMO_HEADER_MANAGER = "app_named_demo_header_manager"
const val AUTH_NAMED_RETROFIT = "auth_named_retrofit"
const val REFRESH_TOKEN_NAMED_RETROFIT = "refresh_token_named_retrofit"
const val APP_NAMED_RETROFIT = "app_named_retrofit"
const val APP_NAMED_TASKS_RETROFIT = "app_named_tasks_retrofit"
const val AUTH_NAMED_HTTP_CLIENT = "auth_named_client"
const val REFRESH_TOKEN_NAMED_HTTP_CLIENT = "refresh_token_named_client"
const val APP_NAMED_HTTP_CLIENT = "app_named_client"
const val APP_NAMED_HTTP_DEMO_CLIENT = "app_named_tasks_client"

val networkModule = module {

    //==============================================================================================
    // url api
    //==============================================================================================
    fun provideAuthBaseUrlServer(configManager: ConfigManager): String {
        return configManager.readAuthBaseUrlServer()
    }

    fun provideAppBaseUrlServer(configManager: ConfigManager): String {
        return configManager.readAppBaseUrlServer()
    }

    //==============================================================================================
    // error factory
    //==============================================================================================
    fun provideErrorResolutionResourceProvider(context: Context): ErrorResolutionResourceProvider {
        return ErrorResolutionResourceProviderImpl(context)
    }

    fun provideErrorResolutionStrategy(
        resourceProvider: ErrorResolutionResourceProvider,
    ): ErrorResolutionStrategy {
        return ErrorResolutionStrategyImpl(resourceProvider)
    }

    fun provideCallAdapterFactory(errorResolutionStrategy: ErrorResolutionStrategy): CallAdapter.Factory {
        return RxHandlingCallAdapterFactory.create(errorResolutionStrategy)
    }

    //==============================================================================================
    //certificate store
    //==============================================================================================
    fun provideCertificateStore(context: Context): CertificateStore {
        return CertificateStoreFactory.createCertificateStore(context)
    }

    fun provideMockResponse(context: Context): MockResponse {
        return MockResponseImpl(context)
    }

    //==============================================================================================
    //token manager
    //==============================================================================================
    fun provideTokenManager(sharedWorker: SharedWorker): TokenManager {
        return TokenManagerImpl(sharedWorker)
    }

    //==============================================================================================
    //user manager
    //==============================================================================================
    fun provideUserManager(sharedWorker: SharedWorker): UserManager {
        return UserManagerImpl(sharedWorker)
    }

    //==============================================================================================
    //header manager
    //==============================================================================================
    fun provideAuthHeaderManager(): HeaderManager {
        return AuthHeaderManagerImpl()
    }

    fun provideRefreshTokenHeaderManager(tokenManager: TokenManager): HeaderManager {
        return RefreshTokenHeaderManagerImpl(tokenManager)
    }

    fun provideAppHeaderManager(tokenManager: TokenManager, host: String): HeaderManager {
        return AppHeaderManagerImpl(tokenManager, URI(host).host)
    }

    fun provideAppDemoHeaderManager(host: String): HeaderManager {
        return AppDemoHeaderManagerImpl(URI(host).host)
    }

    //==============================================================================================
    //converter factory
    //==============================================================================================
    fun provideNullOnEmptyConverterFactory(): NullOnEmptyConverterFactory {
        return NullOnEmptyConverterFactory()
    }

    //==============================================================================================
    // interceptors
    //==============================================================================================
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return InterceptorFactory.createHttpLoggingInterceptor()
    }

    fun provideAuthMockResponseInterceptor(): AuthMockResponseInterceptor {
        return InterceptorFactory.createAuthMockResponseInterceptor("https://wbtrans-auth.wildberries.ru/api/v1")
    }

    fun provideAppMockResponseInterceptor(mockResponse: MockResponse): AppMockResponseInterceptor {
        return InterceptorFactory.createAppMockResponseInterceptor(
            "https://wbtrans-mobile-api.wildberries.ru/api/v1",
            mockResponse
        )
    }

    fun provideAppMetricResponseInterceptor(metric: YandexMetricManager): AppMetricResponseInterceptor {
        return InterceptorFactory.createAppMetricResponseInterceptor(metric)
    }

    fun provideRefreshTokenInterceptor(
        refreshTokenRepository: RefreshTokenRepository,
        headerManager: HeaderManager,
        tokenManager: TokenManager,
    ): RefreshTokenInterceptor {
        return InterceptorFactory.createRefreshTokenInterceptor(
            refreshTokenRepository,
            headerManager,
            tokenManager
        )
    }

    //==============================================================================================
    // OkHttpClient
    //==============================================================================================
    fun provideAuthOkHttpClient(
        certificateStore: CertificateStore,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authMockResponseInterceptor: AuthMockResponseInterceptor
    ): OkHttpClient {
        return OkHttpFactory.createAuthOkHttpClient(
            certificateStore,
            httpLoggingInterceptor,
            authMockResponseInterceptor
        )
    }

    fun createTokenRefreshOkHttpClient(): OkHttpClient {
        return OkHttpFactory.createTokenRefreshOkHttpClient()
    }

    fun provideAppOkHttpClient(
        certificateStore: CertificateStore,
        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        appMetricResponseInterceptor: AppMetricResponseInterceptor,
    ): OkHttpClient {
        return OkHttpFactory.createAppOkHttpClient(
            certificateStore,
            refreshResponseInterceptor,
            httpLoggingInterceptor,
            appMetricResponseInterceptor
        )
    }

    fun provideAppOkHttpDemoClient(
        certificateStore: CertificateStore,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpFactory.createAppOkHttpDemoClient(certificateStore, httpLoggingInterceptor)
    }

    //==============================================================================================
    // retrofit factory
    //==============================================================================================
    fun provideAuthRetrofitFactory(
        apiServer: String,
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
        gsonConverterFactory: GsonConverterFactory,
    ): RetrofitFactory {
        return RetrofitFactory(
            apiServer,
            okHttpClient,
            callAdapterFactory,
            nullOnEmptyConverterFactory,
            gsonConverterFactory
        )
    }

    fun provideRefreshTokenRetrofitFactory(
        apiServer: String,
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): RefreshTokenRetrofitFactory {
        return RefreshTokenRetrofitFactory(
            apiServer,
            okHttpClient,
            gsonConverterFactory
        )
    }

    fun provideAppRetrofitFactory(
        baseUrlServer: String,
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
        gsonConverterFactory: GsonConverterFactory,
    ): RetrofitFactory {
        return RetrofitFactory(
            baseUrlServer,
            okHttpClient,
            callAdapterFactory,
            nullOnEmptyConverterFactory,
            gsonConverterFactory
        )
    }

    fun provideAppTasksRetrofitFactory(
        baseUrlServer: String,
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): RetrofitFactory {
        return RetrofitFactory(
            baseUrlServer,
            okHttpClient,
            callAdapterFactory,
            nullOnEmptyConverterFactory,
            gsonConverterFactory
        )
    }

    //==============================================================================================
    // converters
    //==============================================================================================
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }

    single(named(AUTH_NAMED_BASE_URL)) { provideAuthBaseUrlServer(get()) }
    single(named(APP_NAMED_BASE_URL)) { provideAppBaseUrlServer(get()) }

    single { provideErrorResolutionResourceProvider(get()) }
    single { provideErrorResolutionStrategy(get()) }
    single { provideCallAdapterFactory(get()) }

    single { provideCertificateStore(get()) }

    single { provideMockResponse(get()) }

    single { provideTokenManager(get()) }
    single { provideUserManager(get()) }

    factory(named(AUTH_NAMED_HEADER_MANAGER)) { provideAuthHeaderManager() }
    factory(named(REFRESH_TOKEN_NAMED_HEADER_MANAGER)) { provideRefreshTokenHeaderManager(get()) }
    factory(named(APP_NAMED_HEADER_MANAGER)) {
        provideAppHeaderManager(get(), get(named(APP_NAMED_BASE_URL)))
    }
    factory(named(APP_NAMED_DEMO_HEADER_MANAGER)) {
        provideAppDemoHeaderManager(get(named(APP_NAMED_BASE_URL)))
    }

    single { provideNullOnEmptyConverterFactory() }

    single { provideLoggingInterceptor() }

    single { provideAuthMockResponseInterceptor() }
    single { provideAppMockResponseInterceptor(get()) }
    single { provideAppMetricResponseInterceptor(get()) }

    single { provideRefreshTokenInterceptor(get(), get(named(APP_NAMED_HEADER_MANAGER)), get()) }

    //OkHttp
    single(named(AUTH_NAMED_HTTP_CLIENT)) { provideAuthOkHttpClient(get(), get(), get()) }

    single(named(REFRESH_TOKEN_NAMED_HTTP_CLIENT)) {
        createTokenRefreshOkHttpClient()
    }

    single(named(APP_NAMED_HTTP_CLIENT)) { provideAppOkHttpClient(get(), get(), get(), get()) }

    single(named(APP_NAMED_HTTP_DEMO_CLIENT)) { provideAppOkHttpDemoClient(get(), get()) }

    single { provideGsonConverterFactory() }
    single { provideGson() }

    single(named(AUTH_NAMED_RETROFIT)) {
        provideAuthRetrofitFactory(
            get(named(AUTH_NAMED_BASE_URL)),
            get(named(AUTH_NAMED_HTTP_CLIENT)),
            get(),
            get(),
            get()
        )
    }

    single(named(REFRESH_TOKEN_NAMED_RETROFIT)) {
        provideRefreshTokenRetrofitFactory(
            get(named(AUTH_NAMED_BASE_URL)),
            get(named(REFRESH_TOKEN_NAMED_HTTP_CLIENT)),
            get(),
        )
    }

    single(named(APP_NAMED_RETROFIT)) {
        provideAppRetrofitFactory(
            baseUrlServer = get(named(APP_NAMED_BASE_URL)),
            okHttpClient = get(named(APP_NAMED_HTTP_CLIENT)),
            callAdapterFactory = get(),
            nullOnEmptyConverterFactory = get(),
            gsonConverterFactory = get()
        )
    }

    single(named(APP_NAMED_TASKS_RETROFIT)) {
        provideAppTasksRetrofitFactory(
            baseUrlServer = get(named(APP_NAMED_BASE_URL)),
            okHttpClient = get(named(okHttpClientNamed(tokenManager = get()))),
            callAdapterFactory = get(),
            nullOnEmptyConverterFactory = get(),
            gsonConverterFactory = get(),
        )
    }

}

private fun okHttpClientNamed(tokenManager: TokenManager) =
    if (tokenManager.isDemo()) APP_NAMED_HTTP_DEMO_CLIENT else APP_NAMED_HTTP_CLIENT