package com.wb.logistics.di.module

import com.wb.logistics.network.api.app.AppApi
import com.wb.logistics.network.api.auth.AuthApi
import com.wb.logistics.network.rest.RetrofitAppFactory
import com.wb.logistics.ui.delivery.data.DeliveryApi
import com.wb.logistics.ui.reception.data.ReceptionApi
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {

    fun provideAuthApi(retrofitAppFactory: RetrofitAppFactory): AuthApi {
        return retrofitAppFactory.getApiInterface(AuthApi::class.java)
    }

    fun provideAppApi(retrofitAppFactory: RetrofitAppFactory): AppApi {
        return retrofitAppFactory.getApiInterface(AppApi::class.java)
    }

    fun provideDeliveryApi(retrofit: Retrofit): DeliveryApi {
        return retrofit.create(DeliveryApi::class.java)
    }

    fun provideReceptionApi(retrofit: Retrofit): ReceptionApi {
        return retrofit.create(ReceptionApi::class.java)
    }

    single { provideAuthApi(get(named(AUTH_NAMED_RETROFIT))) }
    single { provideAppApi(get(named(APP_NAMED_RETROFIT))) }
    single { provideDeliveryApi(get()) }
    single { provideReceptionApi(get()) }

}