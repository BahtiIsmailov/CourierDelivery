package com.wb.logistics.di.module

import com.wb.logistics.ui.delivery.data.DeliveryApi
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {
    fun provideUserApi(retrofit: Retrofit): DeliveryApi {
        return retrofit.create(DeliveryApi::class.java)
    }

    single { provideUserApi(get()) }
}