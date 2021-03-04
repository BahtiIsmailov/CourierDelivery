package com.wb.logistics.di.module

import com.wb.logistics.ui.delivery.DeliveryViewModel
import com.wb.logistics.ui.nav.NavigationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { NavigationViewModel(get(), get()) }
    viewModel { DeliveryViewModel(get(), get()) }
}