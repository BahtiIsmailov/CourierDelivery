package com.wb.logistics.di.module

import com.wb.logistics.ui.delivery.data.DeliveryApi
import com.wb.logistics.ui.reception.data.ReceptionApi
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {

    fun provideDeliveryApi(retrofit: Retrofit): DeliveryApi {
        return retrofit.create(DeliveryApi::class.java)
    }

    fun provideReceptionApi(retrofit: Retrofit): ReceptionApi {
        return retrofit.create(ReceptionApi::class.java)
    }

    single { provideDeliveryApi(get()) }
    single { provideReceptionApi(get()) }
}