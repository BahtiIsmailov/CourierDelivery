package com.wb.logistics.app

import android.app.Application
import android.content.Context
import com.wb.logistics.di.module.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger(Level.DEBUG)
            modules(
                listOf(
                    apiModule,
                    databaseModule,
                    networkModule,
                    deliveryRepositoryModule,
                    resourceModule,
                    utilsModule,
                    viewModelModule
                )
            )
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

}