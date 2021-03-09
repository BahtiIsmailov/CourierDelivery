package com.wb.logistics.di.module

import com.wb.logistics.ui.delivery.data.*
import com.wb.logistics.ui.reception.data.ReceptionApi
import com.wb.logistics.ui.reception.data.ReceptionDao
import com.wb.logistics.ui.reception.data.ReceptionRepository
import org.koin.dsl.module

val deliveryRepositoryModule = module {

    fun provideDeliveryRepository(api: DeliveryApi, dao: DeliveryDao): DeliveryRepository {
        return DeliveryRepository(api, dao)
    }

    fun provideReceptionRepository(api: ReceptionApi, dao: ReceptionDao): ReceptionRepository {
        return ReceptionRepository(api, dao)
    }

    single { provideDeliveryRepository(get(), get()) }
    single { provideReceptionRepository(get(), get()) }
}