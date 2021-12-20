package ru.wb.go.di.module

import android.app.Application
import org.koin.dsl.module
import ru.wb.go.ui.auth.AuthResourceProvider
import ru.wb.go.ui.courierbilling.CourierBillingResourceProvider
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataResourceProvider
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorResourceProvider
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResourceProvider
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryResourceProvider
import ru.wb.go.ui.courierdata.CourierDataResourceProvider
import ru.wb.go.ui.courierexpects.CourierExpectsResourceProvider
import ru.wb.go.ui.courierintransit.CourierIntransitResourceProvider
import ru.wb.go.ui.courierloading.CourierLoadingResourceProvider
import ru.wb.go.ui.couriermap.CourierMapResourceProvider
import ru.wb.go.ui.courierorderconfirm.CourierOrderConfirmResourceProvider
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsResourceProvider
import ru.wb.go.ui.courierorders.CourierOrdersResourceProvider
import ru.wb.go.ui.courierordertimer.CourierOrderTimerResourceProvider
import ru.wb.go.ui.courierstartdelivery.CourierStartDeliveryResourceProvider
import ru.wb.go.ui.courierunloading.CourierUnloadingResourceProvider
import ru.wb.go.ui.courierwarehouses.CourierWarehousesResourceProvider
import ru.wb.go.ui.splash.AppResourceProvider

val resourceModule = module {

    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideUserDataResourceProvider(application: Application): CourierDataResourceProvider {
        return CourierDataResourceProvider(application)
    }

    fun provideCouriersCompleteRegistrationResourceProvider(application: Application): CourierExpectsResourceProvider {
        return CourierExpectsResourceProvider(application)
    }

    fun provideCourierOrderDetailsResourceProvider(application: Application): CourierOrderDetailsResourceProvider {
        return CourierOrderDetailsResourceProvider(application)
    }

    fun provideCourierWarehouseResourceProvider(application: Application): CourierWarehousesResourceProvider {
        return CourierWarehousesResourceProvider(application)
    }

    fun provideCourierOrderResourceProvider(application: Application): CourierOrdersResourceProvider {
        return CourierOrdersResourceProvider(application)
    }

    fun provideCourierCarNumberResourceProvider(application: Application): CourierCarNumberResourceProvider {
        return CourierCarNumberResourceProvider(application)
    }

    fun provideCourierOrderConfirmResourceProvider(application: Application): CourierOrderConfirmResourceProvider {
        return CourierOrderConfirmResourceProvider(application)
    }

    fun provideCourierOrderTimerResourceProvider(application: Application): CourierOrderTimerResourceProvider {
        return CourierOrderTimerResourceProvider(application)
    }

    fun provideCourierBillingResourceProvider(application: Application): CourierBillingResourceProvider {
        return CourierBillingResourceProvider(application)
    }


    fun provideCourierScannerLoadingResourceProvider(application: Application): CourierLoadingResourceProvider {
        return CourierLoadingResourceProvider(application)
    }

    fun provideCourierIntransitResourceProvider(application: Application): CourierIntransitResourceProvider {
        return CourierIntransitResourceProvider(application)
    }

    fun provideCourierUnloadingResourceProvider(application: Application): CourierUnloadingResourceProvider {
        return CourierUnloadingResourceProvider(application)
    }

    fun provideCourierCompleteDeliveryResourceProvider(application: Application): CourierCompleteDeliveryResourceProvider {
        return CourierCompleteDeliveryResourceProvider(application)
    }

    fun provideCourierStartDeliveryResourceProvider(application: Application): CourierStartDeliveryResourceProvider {
        return CourierStartDeliveryResourceProvider(application)
    }

    fun provideCourierMapResourceProvider(application: Application): CourierMapResourceProvider {
        return CourierMapResourceProvider(application)
    }

    fun provideCourierBillingAccountDataResourceProvider(application: Application): CourierBillingAccountDataResourceProvider {
        return CourierBillingAccountDataResourceProvider(application)
    }

    fun provideCourierBillingAccountSelectorResourceProvider(application: Application): CourierBillingAccountSelectorResourceProvider {
        return CourierBillingAccountSelectorResourceProvider(application)
    }

    fun provideTemporaryPasswordResourceProvider(application: Application): AuthResourceProvider {
        return AuthResourceProvider(application)
    }

    single { provideAppResourceProvider(get()) }
    single { provideUserDataResourceProvider(get()) }
    single { provideCouriersCompleteRegistrationResourceProvider(get()) }
    single { provideCourierOrderDetailsResourceProvider(get()) }
    single { provideCourierWarehouseResourceProvider(get()) }
    single { provideCourierOrderResourceProvider(get()) }
    single { provideCourierCarNumberResourceProvider(get()) }
    single { provideCourierOrderConfirmResourceProvider(get()) }
    single { provideCourierOrderTimerResourceProvider(get()) }
    single { provideCourierScannerLoadingResourceProvider(get()) }
    single { provideCourierIntransitResourceProvider(get()) }
    single { provideCourierUnloadingResourceProvider(get()) }
    single { provideCourierCompleteDeliveryResourceProvider(get()) }
    single { provideCourierStartDeliveryResourceProvider(get()) }
    single { provideCourierMapResourceProvider(get()) }
    single { provideCourierBillingResourceProvider(get()) }
    single { provideCourierBillingAccountDataResourceProvider(get()) }
    single { provideCourierBillingAccountSelectorResourceProvider(get()) }

    single { provideTemporaryPasswordResourceProvider(get()) }

}