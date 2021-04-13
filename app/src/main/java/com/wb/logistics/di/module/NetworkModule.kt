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
import com.wb.logistics.network.rest.RetrofitAppFactory
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

const val AUTH_NAMED_API = "auth_named_api"
const val APP_NAMED_API = "app_named_api"
const val AUTH_NAMED_MANAGER = "auth_named_manager"
const val APP_NAMED_MANAGER = "app_named_manager"
const val AUTH_NAMED_INTERCEPTOR = "auth_named_interceptor"
const val APP_NAMED_INTERCEPTOR = "app_named_interceptor"
const val AUTH_NAMED_RETROFIT = "auth_named_retrofit"
const val APP_NAMED_RETROFIT = "app_named_retrofit"
const val AUTH_NAMED_CLIENT = "auth_named_client"
const val APP_NAMED_CLIENT = "app_named_client"

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
        //authRepository: AuthRepository
    ): ErrorResolutionStrategy {
        return ErrorResolutionStrategyImpl(resourceProvider) //, authRepository
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

    fun provideAppHeaderManager(tokenManager: TokenManager, host: String): HeaderManager {
        return AppHeaderManagerImpl(tokenManager.bearerToken(), URI(host).host)
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
    fun provideLoggerInterceptor(): HttpLoggingInterceptor {
        return InterceptorFactory.createHttpLoggingInterceptor()
    }

    fun provideAuthHeaderInterceptor(headerManager: HeaderManager): HeaderRequestInterceptor {
        return InterceptorFactory.createHeaderRequestInterceptor(headerManager)
    }

    fun provideAppHeaderInterceptor(headerManager: HeaderManager): HeaderRequestInterceptor {
        return InterceptorFactory.createHeaderRequestInterceptor(headerManager)
    }

    //==============================================================================================
    // OkHttpClient
    //==============================================================================================
    fun provideAuthOkHttpClient(
        certificateStore: CertificateStore,
        requestInterceptor: HeaderRequestInterceptor,
        httpLoggerInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpFactory.createAuthOkHttpClient(
            certificateStore,
            requestInterceptor,
            httpLoggerInterceptor
        )
    }

    fun provideAppOkHttpClient(
        certificateStore: CertificateStore,
        requestInterceptor: HeaderRequestInterceptor,
        httpLoggerInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpFactory.createAuthOkHttpClient(
            certificateStore,
            requestInterceptor,
            httpLoggerInterceptor
        )
    }

    //==============================================================================================
    // retrofit factory
    //==============================================================================================
    fun provideAuthRetrofitAuthFactory(
        apiServer: String,
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): RetrofitAppFactory {
        return RetrofitAppFactory(
            apiServer,
            okHttpClient,
            callAdapterFactory,
            nullOnEmptyConverterFactory,
            gsonConverterFactory
        )
    }

    fun provideAppRetrofitAuthFactory(
        apiServer: String,
        okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        nullOnEmptyConverterFactory: NullOnEmptyConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): RetrofitAppFactory {
        return RetrofitAppFactory(
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

    single(named(AUTH_NAMED_API)) { provideAuthServer(get()) }
    single(named(APP_NAMED_API)) { provideAppServer(get()) }

    single { provideErrorResolutionResourceProvider(get()) }
    single { provideErrorResolutionStrategy(get()) }
    single { provideCallAdapterFactory(get()) }

    single { provideCertificateStore(get()) }

    single { provideTokenManager(get()) }
    single { provideUserManager(get()) }

    single(named(AUTH_NAMED_MANAGER)) { provideAuthHeaderManager() }
    single(named(APP_NAMED_MANAGER)) { provideAppHeaderManager(get(), get(named(APP_NAMED_API))) }

    single { provideNullOnEmptyConverterFactory() }

    single { provideLoggerInterceptor() }
    single(named(AUTH_NAMED_INTERCEPTOR)) {
        provideAuthHeaderInterceptor(
            get(
                named(
                    AUTH_NAMED_MANAGER
                )
            )
        )
    }
    single(named(APP_NAMED_INTERCEPTOR)) { provideAppHeaderInterceptor(get(named(APP_NAMED_MANAGER))) }

    single(named(AUTH_NAMED_CLIENT)) {
        provideAuthOkHttpClient(
            get(),
            get(named(AUTH_NAMED_INTERCEPTOR)),
            get()
        )
    }
    single(named(APP_NAMED_CLIENT)) {
        provideAppOkHttpClient(
            get(),
            get(named(APP_NAMED_INTERCEPTOR)),
            get()
        )
    }

    single { provideGsonConverterFactory() }
    single { provideGson() }

    single(named(AUTH_NAMED_RETROFIT)) {
        provideAuthRetrofitAuthFactory(
            get(named(AUTH_NAMED_API)),
            get(named(AUTH_NAMED_CLIENT)),
            get(),
            get(),
            get()
        )
    }

    single(named(APP_NAMED_RETROFIT)) {
        provideAppRetrofitAuthFactory(
            get(named(APP_NAMED_API)),
            get(named(APP_NAMED_CLIENT)),
            get(),
            get(),
            get()
        )
    }


}