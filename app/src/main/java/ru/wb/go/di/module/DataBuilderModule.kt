package ru.wb.go.di.module

import org.koin.dsl.module
import ru.wb.go.ui.courierbilling.CourierBillingDataBuilder
import ru.wb.go.ui.courierbilling.CourierBillingDataBuilderImpl
import ru.wb.go.ui.courierbilling.CourierBillingResourceProvider
import ru.wb.go.ui.courierorders.CourierOrdersDataBuilder
import ru.wb.go.ui.courierorders.CourierOrdersDataBuilderImpl
import ru.wb.go.ui.courierorders.CourierOrdersResourceProvider
import ru.wb.go.utils.time.TimeFormatter

val dataBuilderModule = module {

    fun provideCourierOrderDataBuilder(
        resourceProvider: CourierOrdersResourceProvider,
    ): CourierOrdersDataBuilder {
        return CourierOrdersDataBuilderImpl(resourceProvider)
    }

    fun provideCourierBillingDataBuilder(
        resourceProvider: CourierBillingResourceProvider,
        timeFormatter: TimeFormatter,
    ): CourierBillingDataBuilder {
        return CourierBillingDataBuilderImpl(resourceProvider, timeFormatter)
    }

    single { provideCourierOrderDataBuilder(get()) }
    single { provideCourierBillingDataBuilder(get(), get()) }

}