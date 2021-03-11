package com.wb.logistics.di.module

import com.wb.logistics.ui.config.ConfigViewModel
import com.wb.logistics.ui.delivery.DeliveryViewModel
import com.wb.logistics.ui.nav.NavigationViewModel
import com.wb.logistics.ui.reception.ReceptionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ConfigViewModel(get(), get()) }
    viewModel { NavigationViewModel(get(), get()) }
    viewModel { DeliveryViewModel(get(), get()) }
    viewModel { ReceptionViewModel(get(), get()) }
}