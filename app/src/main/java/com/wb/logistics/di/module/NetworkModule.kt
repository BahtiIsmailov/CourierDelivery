package com.wb.logistics.di.module

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wb.logistics.network.NullOnEmptyConverterFactory
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.client.OkHttpFactory
import com.wb.logistics.network.domain.HeaderRequestInterceptor
import com.wb.logistics.network.domain.InterceptorFactory
import com.wb.logistics.network.headers.HeaderManager
import com.wb.logistics.network.rest.RetrofitAppFactory
import com.wb.logistics.network.rx.*
import com.wb.logistics.utils.managers.ConfigManager
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
    fun provideCallAdapterFactoryResourceProvider(context: Context): CallAdapterFactoryResourceProvider {
        return CallAdapterFactoryResourceProviderImpl(context)
    }

    fun provideApiErrorsFactory(resourceProvider: CallAdapterFactoryResourceProvider): ErrorResolutionStrategy {
        return ErrorResolutionStrategyImpl(resourceProvider)
    }

    fun provideCallAdapterFactory(errorResolutionStrategy: ErrorResolutionStrategy): CallAdapter.Factory {
        return RxHandlingCallAdapterFactory.create(errorResolutionStrategy)
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

    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }

    single(named(AUTH_NAMED)) { provideAuthServer(get()) }

    single { provideCallAdapterFactoryResourceProvider(get()) }
    single { provideApiErrorsFactory(get()) }
    single { provideCallAdapterFactory(get()) }

    single { provideLoggerInterceptor() }
    single { provideHeaderInterceptor(get()) }
    single { provideOkHttpClient(get(), get(), get()) }

    single { provideGsonConverterFactory() }

    single { provideRetrofitAuthFactory(get(), get(), get(), get(), get()) }

    single { provideGson() }

}