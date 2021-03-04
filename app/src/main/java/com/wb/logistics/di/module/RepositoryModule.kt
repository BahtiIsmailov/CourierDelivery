package com.wb.logistics.di.module

import com.wb.logistics.ui.delivery.data.DeliveryApi
import com.wb.logistics.ui.delivery.data.DeliveryDao
import com.wb.logistics.ui.delivery.data.DeliveryRepository
import org.koin.dsl.module

val deliveryRepositoryModule = module {
    fun provideUserRepository(api: DeliveryApi, dao: DeliveryDao): DeliveryRepository {
        return DeliveryRepository(api, dao)
    }

    single { provideUserRepository(get(), get()) }
}