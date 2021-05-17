package com.wb.logistics.app

import android.app.Application
import android.content.Context
import com.wb.logistics.di.module.*
import com.wb.logistics.network.monitor.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initDI()
        initNetworkMonitor()
    }

    private fun initDI() {
        startKoin {
            androidContext(this@App)
            androidLogger(Level.DEBUG)
            modules(
                listOf(
                    apiModule,
                    databaseModule,
                    dataBuilderModule,
                    interactorModule,
                    networkModule,
                    deliveryRepositoryModule,
                    resourceModule,
                    rxModule,
                    utilsModule,
                    viewModelModule
                )
            )
        }
    }

    private fun initNetworkMonitor() {
        NetworkMonitor(this).startNetworkCallback()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onTerminate(){
        super.onTerminate()
        NetworkMonitor(this).stopNetworkCallback()
    }

}