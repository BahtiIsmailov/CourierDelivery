package ru.wb.go.di.module

import android.app.Application
import org.koin.dsl.module
import ru.wb.go.ui.app.AppResourceProvider
import ru.wb.go.ui.auth.AuthResourceProvider
import ru.wb.go.ui.courierbilling.CourierBillingResourceProvider
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataResourceProvider
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorResourceProvider
import ru.wb.go.ui.courierbilllingcomplete.CourierBillingCompleteResourceProvider
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResourceProvider
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryResourceProvider
import ru.wb.go.ui.courierdata.CourierDataResourceProvider
import ru.wb.go.ui.courierdataexpects.CourierDataExpectsResourceProvider
import ru.wb.go.ui.courierdatatype.CourierDataTypeResourceProvider
import ru.wb.go.ui.courierintransit.CourierIntransitResourceProvider
import ru.wb.go.ui.courierintransitofficescanner.CourierIntransitOfficeScannerResourceProvider
import ru.wb.go.ui.courierloader.CourierLoaderResourceProvider
import ru.wb.go.ui.courierloading.CourierLoadingResourceProvider
import ru.wb.go.ui.courierordertimer.CourierOrderTimerResourceProvider
import ru.wb.go.ui.courierstartdelivery.CourierStartDeliveryResourceProvider
import ru.wb.go.ui.courierunloading.CourierUnloadingResourceProvider
import ru.wb.go.ui.courierversioncontrol.CourierVersionControlResourceProvider
import ru.wb.go.ui.courierwarehouses.CourierWarehousesResourceProvider
import ru.wb.go.ui.settings.domain.SettingsResourceProvider

val resourceModule = module {

    fun provideAppResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideCourierDataTypeResourceProvider(application: Application): CourierDataTypeResourceProvider {
        return CourierDataTypeResourceProvider(application)
    }

    fun provideCouriersCompleteRegistrationResourceProvider(application: Application): CourierDataExpectsResourceProvider {
        return CourierDataExpectsResourceProvider(application)
    }

    fun provideCourierVersionControlResourceProvider(application: Application): CourierVersionControlResourceProvider {
        return CourierVersionControlResourceProvider(application)
    }

    fun provideCourierWarehouseResourceProvider(application: Application): CourierWarehousesResourceProvider {
        return CourierWarehousesResourceProvider(application)
    }


    fun provideCourierCarNumberResourceProvider(application: Application): CourierCarNumberResourceProvider {
        return CourierCarNumberResourceProvider(application)
    }

    fun provideCourierLoaderResourceProvider(application: Application): CourierLoaderResourceProvider {
        return CourierLoaderResourceProvider(application)
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

    fun provideCourierIntransitOfficeScannerResourceProvider(application: Application): CourierIntransitOfficeScannerResourceProvider {
        return CourierIntransitOfficeScannerResourceProvider(application)
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

    fun provideCourierBillingAccountDataResourceProvider(application: Application): CourierBillingAccountDataResourceProvider {
        return CourierBillingAccountDataResourceProvider(application)
    }

    fun provideCourierBillingAccountSelectorResourceProvider(application: Application): CourierBillingAccountSelectorResourceProvider {
        return CourierBillingAccountSelectorResourceProvider(application)
    }

    fun provideTemporaryPasswordResourceProvider(application: Application): AuthResourceProvider {
        return AuthResourceProvider(application)
    }

    fun provideCourierBillingCompleteResourceProvider(application: Application): CourierBillingCompleteResourceProvider {
        return CourierBillingCompleteResourceProvider(application)
    }

    fun provideCourierDataResourceProvider(application: Application): CourierDataResourceProvider {
        return CourierDataResourceProvider(application)
    }

    fun provideSettingsResourceProvider(application: Application): SettingsResourceProvider {
        return SettingsResourceProvider(application)
    }

    single { provideAppResourceProvider(get()) }
    single { provideCourierDataTypeResourceProvider(get()) }
    single { provideCouriersCompleteRegistrationResourceProvider(get()) }
    single { provideCourierVersionControlResourceProvider(get()) }
    single { provideCourierWarehouseResourceProvider(get()) }
    single { provideCourierCarNumberResourceProvider(get()) }
    single { provideCourierLoaderResourceProvider(get()) }
    single { provideCourierOrderTimerResourceProvider(get()) }
    single { provideCourierScannerLoadingResourceProvider(get()) }
    single { provideCourierIntransitResourceProvider(get()) }
    single { provideCourierIntransitOfficeScannerResourceProvider(get()) }
    single { provideCourierUnloadingResourceProvider(get()) }
    single { provideCourierCompleteDeliveryResourceProvider(get()) }
    single { provideCourierStartDeliveryResourceProvider(get()) }
    single { provideCourierBillingResourceProvider(get()) }
    single { provideCourierBillingAccountDataResourceProvider(get()) }
    single { provideCourierBillingAccountSelectorResourceProvider(get()) }
    single { provideCourierBillingCompleteResourceProvider(get()) }

    single { provideTemporaryPasswordResourceProvider(get()) }
    single { provideCourierDataResourceProvider(get()) }

    single { provideSettingsResourceProvider(get()) }

}