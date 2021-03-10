package com.wb.logistics.di.module

import android.app.Application
import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.wb.logistics.BuildConfig
import com.wb.logistics.app.BASE_URL_AUTH
import com.wb.logistics.network.NullOnEmptyConverterFactory
import com.wb.logistics.network.certificate.CertificateStore
import com.wb.logistics.network.client.OkHttpFactory
import com.wb.logistics.network.domain.HeaderRequestInterceptor
import com.wb.logistics.network.domain.InterceptorFactory
import com.wb.logistics.network.headers.HeaderManager
import com.wb.logistics.network.rest.RetrofitAppFactory
import com.wb.logistics.network.rx.*
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {

    //==============================================================================================
    // url api
    //==============================================================================================
    fun provideAuthServer(): String {
        return BASE_URL_AUTH
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


    fun provideHttpLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
    }

    fun provideCache(application: Application): Cache {
        val cacheSize = 10 * 1024 * 1024
        return Cache(application.cacheDir, cacheSize.toLong())
    }

    fun provideHttpClient(loggerInterceptor: Interceptor, cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggerInterceptor)
            .cache(cache)
            .build()
    }

    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }


    fun provideRetrofit(factory: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_AUTH)
            .addConverterFactory(GsonConverterFactory.create(factory))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
    }

    single<String>(named("auth")) { provideAuthServer() }
//        .client(get(named("auth")))

    single { provideCallAdapterFactoryResourceProvider(get()) }
    single { provideApiErrorsFactory(get()) }
    single { provideCallAdapterFactory(get()) }

    single { provideLoggerInterceptor() }
    single { provideHeaderInterceptor(get()) }
    single { provideOkHttpClient(get(), get(), get()) }

    single { provideGsonConverterFactory() }

    single { provideRetrofitAuthFactory(get(), get(), get(), get(), get()) }

    single { provideHttpLoggingInterceptor() }
    single { provideCache(androidApplication()) }
    single { provideHttpClient(get(), get()) }
    single { provideGson() }
    single { provideRetrofit(get(), get()) }

}