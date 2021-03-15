package com.wb.logistics.di.module

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wb.logistics.network.NullOnEmptyConverterFactory
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.certificate.CertificateStoreFactory
import com.wb.logistics.network.client.OkHttpFactory
import com.wb.logistics.network.domain.HeaderRequestInterceptor
import com.wb.logistics.network.domain.InterceptorFactory
import com.wb.logistics.network.exceptions.ErrorResolutionResourceProvider
import com.wb.logistics.network.exceptions.ErrorResolutionResourceProviderImpl
import com.wb.logistics.network.exceptions.ErrorResolutionStrategy
import com.wb.logistics.network.exceptions.ErrorResolutionStrategyImpl
import com.wb.logistics.network.headers.HeaderManager
import com.wb.logistics.network.headers.HeaderManagerImpl
import com.wb.logistics.network.headers.TokenManager
import com.wb.logistics.network.headers.TokenManagerImpl
import com.wb.logistics.network.rest.RetrofitAppFactory
import com.wb.logistics.network.rx.RxHandlingCallAdapterFactory
import com.wb.logistics.utils.managers.ConfigManager
import com.wb.logistics.utils.prefs.SharedWorker
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.converter.gson.GsonConverterFactory

const val AUTH_NAMED = "auth"

val networkModule = module {

    //==============================================================================================
    // url api
    //==============================================================================================
    fun provideAuthServer(configManager: ConfigManager): String {
        return configManager.readAuthServerUrl()
    }

    //==============================================================================================
    // error factory
    //==============================================================================================
    fun provideCallAdapterFactoryResourceProvider(context: Context): ErrorResolutionResourceProvider {
        return ErrorResolutionResourceProviderImpl(context)
    }

    fun provideApiErrorsFactory(resourceProvider: ErrorResolutionResourceProvider): ErrorResolutionStrategy {
        return ErrorResolutionStrategyImpl(
            resourceProvider
        )
    }

    fun provideCallAdapterFactory(errorResolutionStrategy: ErrorResolutionStrategy): CallAdapter.Factory {
        return RxHandlingCallAdapterFactory.create(errorResolutionStrategy)
    }

    //==============================================================================================
    //store
    //==============================================================================================
    fun provideCertificateStore(context: Context): CertificateStore {
        return CertificateStoreFactory.createCertificateStore(context)
    }

    //==============================================================================================
    //header manager
    //==============================================================================================
    fun provideHeaderFactory(tokenManager: TokenManager): HeaderManager {
        return HeaderManagerImpl(tokenManager)
    }

    //==============================================================================================
    //token manager
    //==============================================================================================
    fun provideTokenManager(sharedWorker: SharedWorker): TokenManager {
        return TokenManagerImpl(sharedWorker)
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

    fun provideHeaderInterceptor(headerManager: HeaderManager): HeaderRequestInterceptor {
        return InterceptorFactory.createHeaderRequestInterceptor(headerManager)
    }

    //==============================================================================================
    // OkHttpClient
    //==============================================================================================
    fun provideOkHttpClient(
        certificateStore: CertificateStore,
        requestInterceptor: HeaderRequestInterceptor,
        httpLoggerInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpFactory.createOkHttpClient(
            certificateStore,
            requestInterceptor,
            httpLoggerInterceptor
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

    //==============================================================================================
    // retrofit factory
    //==============================================================================================
    fun provideRetrofitAuthFactory(
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

    single(named(AUTH_NAMED)) { provideAuthServer(get()) }

    single { provideCallAdapterFactoryResourceProvider(get()) }
    single { provideApiErrorsFactory(get()) }
    single { provideCallAdapterFactory(get()) }

    single { provideLoggerInterceptor() }
    single { provideHeaderInterceptor(get()) }
    single { provideOkHttpClient(get(), get(), get()) }

    single { provideCertificateStore(get()) }
    single { provideHeaderFactory(get()) }
    single { provideTokenManager(get()) }
    single { provideNullOnEmptyConverterFactory() }

    single { provideGsonConverterFactory() }

    single { provideRetrofitAuthFactory(get(named(AUTH_NAMED)), get(), get(), get(), get()) }

    single { provideGson() }

}