package com.wb.logistics.di.module

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wb.logistics.network.NullOnEmptyConverterFactory
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.certificate.CertificateStoreFactory
import com.wb.logistics.network.client.OkHttpFactory
import com.wb.logistics.network.exceptions.ErrorResolutionResourceProvider
import com.wb.logistics.network.exceptions.ErrorResolutionResourceProviderImpl
import com.wb.logistics.network.exceptions.ErrorResolutionStrategy
import com.wb.logistics.network.exceptions.ErrorResolutionStrategyImpl
import com.wb.logistics.network.headers.*
import com.wb.logistics.network.rest.RefreshTokenRetrofitFactory
import com.wb.logistics.network.rest.RetrofitFactory
import com.wb.logistics.network.rx.RxHandlingCallAdapterFactory
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.network.token.TokenManagerImpl
import com.wb.logistics.network.token.UserManager
import com.wb.logistics.network.token.UserManagerImpl
import com.wb.logistics.utils.managers.ConfigManager
import com.wb.logistics.utils.prefs.SharedWorker
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.converter.gson.GsonConverterFactory
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

    fun provideRefreshTokenInterceptor(
        refreshTokenRepository: RefreshTokenRepository,
        headerManager: HeaderManager,
        tokenManager: TokenManager,
    ): RefreshTokenInterceptor {
        return InterceptorFactory.createRefreshTokenInterceptor(refreshTokenRepository,
            headerManager,
            tokenManager)
    }

    //==============================================================================================
    // OkHttpClient
    //==============================================================================================
    fun provideAuthOkHttpClient(
        certificateStore: CertificateStore,
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpFactory.createAuthOkHttpClient(
            certificateStore,
            httpLoggingInterceptor
        )
    }

    fun createTokenRefreshOkHttpClient(): OkHttpClient {
        return OkHttpFactory.createTokenRefreshOkHttpClient()
    }

    fun provideAppOkHttpClient(
        certificateStore: CertificateStore,
        refreshResponseInterceptor: RefreshTokenInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpFactory.createAppOkHttpClient(
            certificateStore,
            refreshResponseInterceptor,
            httpLoggingInterceptor,
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

    single { provideTokenManager(get()) }
    single { provideUserManager(get()) }

    factory(named(AUTH_NAMED_HEADER_MANAGER)) { provideAuthHeaderManager() }
    factory(named(REFRESH_TOKEN_NAMED_HEADER_MANAGER)) { provideRefreshTokenHeaderManager(get()) } //get(named(AUTH_NAMED_HOST))
    factory(named(APP_NAMED_HEADER_MANAGER)) {
        provideAppHeaderManager(get(), get(named(APP_NAMED_HOST)))
    }

    single { provideNullOnEmptyConverterFactory() }

    single { provideLoggingInterceptor() }

    single { provideRefreshTokenInterceptor(get(), get(named(APP_NAMED_HEADER_MANAGER)), get()) }

    single(named(AUTH_NAMED_HTTP_CLIENT)) { provideAuthOkHttpClient(get(), get()) }

    single(named(REFRESH_TOKEN_NAMED_HTTP_CLIENT)) {
        createTokenRefreshOkHttpClient()
    }

    single(named(APP_NAMED_HTTP_CLIENT)) { provideAppOkHttpClient(get(), get(), get()) }

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