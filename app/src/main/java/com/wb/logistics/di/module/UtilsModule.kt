package com.wb.logistics.di.module

import android.app.Application
import com.wb.logistics.utils.managers.DeviceManager
import com.wb.logistics.utils.managers.DeviceManagerImpl
import com.wb.logistics.utils.prefs.SharedWorker
import com.wb.logistics.utils.prefs.SharedWorkerImpl
import org.koin.dsl.module

val utilsModule = module {

    fun provideSharedWorker(application: Application): SharedWorker {
        return SharedWorkerImpl(application)
    }

    fun provideDeviceManager(application: Application): DeviceManager {
        return DeviceManagerImpl(application)
    }

    single { provideSharedWorker(get()) }
    single { provideDeviceManager(get()) }
}