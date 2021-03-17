package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.ui.auth.AuthResourceProvider
import com.wb.logistics.ui.res.AppResourceProvider
import org.koin.dsl.module

val resourceModule = module {
    fun provideResourceProvider(application: Application): AppResourceProvider {
        return AppResourceProvider(application)
    }

    fun provideTemporaryPasswordResourceProvider(application: Application): AuthResourceProvider {
        return AuthResourceProvider(application)
    }

    single { provideResourceProvider(get()) }
    single { provideTemporaryPasswordResourceProvider(get()) }
}