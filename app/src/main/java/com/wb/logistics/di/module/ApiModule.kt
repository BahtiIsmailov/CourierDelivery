package com.wb.logistics.di.module

import com.wb.logistics.network.api.AuthApi
import com.wb.logistics.network.rest.RetrofitAppFactory
import com.wb.logistics.ui.delivery.data.DeliveryApi
import com.wb.logistics.ui.reception.data.ReceptionApi
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {

    fun provideAuthApi(retrofitAppFactory: RetrofitAppFactory): AuthApi {
        return retrofitAppFactory.getApiInterface(AuthApi::class.java)
    }

    fun provideDeliveryApi(retrofit: Retrofit): DeliveryApi {
        return retrofit.create(DeliveryApi::class.java)
    }

    fun provideReceptionApi(retrofit: Retrofit): ReceptionApi {
        return retrofit.create(ReceptionApi::class.java)
    }

    single { provideAuthApi(get()) }
    single { provideDeliveryApi(get()) }
    single { provideReceptionApi(get()) }
}