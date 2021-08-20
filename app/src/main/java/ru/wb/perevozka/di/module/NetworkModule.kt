package ru.wb.perevozka.di.module

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
import ru.wb.perevozka.network.NullOnEmptyConverterFactory
import ru.wb.perevozka.network.certificate.CertificateStore
import ru.wb.perevozka.network.certificate.CertificateStoreFactory
import ru.wb.perevozka.network.client.OkHttpFactory
import ru.wb.perevozka.network.exceptions.ErrorResolutionResourceProvider
import ru.wb.perevozka.network.exceptions.ErrorResolutionResourceProviderImpl
import ru.wb.perevozka.network.exceptions.ErrorResolutionStrategy
import ru.wb.perevozka.network.exceptions.ErrorResolutionStrategyImpl
import ru.wb.perevozka.network.headers.*
import ru.wb.perevozka.network.rest.RefreshTokenRetrofitFactory
import ru.wb.perevozka.network.rest.RetrofitFactory
import ru.wb.perevozka.network.rx.RxHandlingCallAdapterFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.network.token.TokenManagerImpl
import ru.wb.perevozka.network.token.UserManager
import ru.wb.perevozka.network.token.UserManagerImpl
import ru.wb.perevozka.reader.MockResponse
import ru.wb.perevozka.reader.MockResponseImpl
import ru.wb.perevozka.utils.managers.ConfigManager
import ru.wb.perevozka.utils.prefs.SharedWorker
import java.net.URI

const val AUTH_NAMED_HOST = "auth_named_host"
const val APP_NAMED_HOST = "app_named_host"
const val AUTH_NAMED_HEADER_MANAGER = "auth_named_header_manager"
const val REFRESH_TOKEN_NAMED_HEADER_MANAGER = "refresh_token_named_header_manager"
const val APP_NAMED_HEADER_MANAGER = "app_named_header_manager"
const val AUTH_NAMED_RETROFIT = "auth_named_retrofit"
const val REFRESH_TOKEN_NAMED_RETROFIT = "refresh_token_named_retrofit"
const val APP_NAMED_RETROFIT = "app_named_retrofit"
const val AUTH_NAMED_HTTP_CLIENT = "auth_named_client"
const val REFRESH_TOKEN_NAMED_HTTP_CLIENT = "refresh_token_named_client"
const val APP_NAMED_HTTP_CLIENT = "app_named_client"

val networkModule = module {

    //==============================================================================================
    // url api
    //==============================================================================================
    fun provideAuthServer(configManager: ConfigManager): String {
        return configManager.readAuthServerUrl()
    }

    fun provideAppServer(configManager: ConfigManager): String {
        return configManager.readAppServerUrl()
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
        appMockResponseInterceptor: AppMockResponseInterceptor
    ): OkHttpClient {
        return OkHttpFactory.createAppOkHttpClient(
            certificateStore,
            refreshResponseInterceptor,
            httpLoggingInterceptor,
            appMockResponseInterceptor
        )
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

    //==============================================================================================
    // converters
    //==============================================================================================
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }

    single(named(AUTH_NAMED_HOST)) { provideAuthServer(get()) }
    single(named(APP_NAMED_HOST)) { provideAppServer(get()) }

    single { provideErrorResolutionResourceProvider(get()) }
    single { provideErrorResolutionStrategy(get()) }
    single { provideCallAdapterFactory(get()) }

    single { provideCertificateStore(get()) }

    single { provideMockResponse(get()) }

    single { provideTokenManager(get()) }
    single { provideUserManager(get()) }

    factory(named(AUTH_NAMED_HEADER_MANAGER)) { provideAuthHeaderManager() }
    factory(named(REFRESH_TOKEN_NAMED_HEADER_MANAGER)) { provideRefreshTokenHeaderManager(get()) } //get(named(AUTH_NAMED_HOST))
    factory(named(APP_NAMED_HEADER_MANAGER)) {
        provideAppHeaderManager(get(), get(named(APP_NAMED_HOST)))
    }

    single { provideNullOnEmptyConverterFactory() }

    single { provideLoggingInterceptor() }

    single { provideAuthMockResponseInterceptor() }
    single { provideAppMockResponseInterceptor(get()) }

    single { provideRefreshTokenInterceptor(get(), get(named(APP_NAMED_HEADER_MANAGER)), get()) }

    single(named(AUTH_NAMED_HTTP_CLIENT)) { provideAuthOkHttpClient(get(), get(), get()) }

    single(named(REFRESH_TOKEN_NAMED_HTTP_CLIENT)) {
        createTokenRefreshOkHttpClient()
    }

    single(named(APP_NAMED_HTTP_CLIENT)) { provideAppOkHttpClient(get(), get(), get(), get()) }

    single { provideGsonConverterFactory() }
    single { provideGson() }

    single(named(AUTH_NAMED_RETROFIT)) {
        provideAuthRetrofitFactory(
            get(named(AUTH_NAMED_HOST)),
            get(named(AUTH_NAMED_HTTP_CLIENT)),
            get(),
            get(),
            get()
        )
    }

    single(named(REFRESH_TOKEN_NAMED_RETROFIT)) {
        provideRefreshTokenRetrofitFactory(
            get(named(AUTH_NAMED_HOST)),
            get(named(REFRESH_TOKEN_NAMED_HTTP_CLIENT)),
            get(),
        )
    }

    single(named(APP_NAMED_RETROFIT)) {
        provideAppRetrofitFactory(
            get(named(APP_NAMED_HOST)),
            get(named(APP_NAMED_HTTP_CLIENT)),
            get(),
            get(),
            get()
        )
    }

}