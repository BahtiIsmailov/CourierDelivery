package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.ui.res.ResourceProvider
import org.koin.dsl.module

val resourceModule = module {
    fun provideResourceProvider(application: Application): ResourceProvider {
        return ResourceProvider(application)
    }

    single { provideResourceProvider(get()) }
}