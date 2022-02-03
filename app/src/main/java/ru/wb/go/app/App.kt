package ru.wb.go.app

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ru.wb.go.BuildConfig
import ru.wb.go.di.module.*
import ru.wb.go.network.monitor.NetworkMonitor

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initDI()
        initNetworkMonitor()
        initFirebaseAnalytics()
        initYandexMetric(this)
    }

    private fun initDI() {
        startKoin {
            androidContext(this@App)
            androidLogger(Level.ERROR)
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

    private fun initFirebaseAnalytics() {
        //val isEnable: Boolean = !BuildConfig.DEBUG
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
    }

    private fun initNetworkMonitor() {
        NetworkMonitor(this).startNetworkCallback()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onTerminate() {
        super.onTerminate()
        NetworkMonitor(this).stopNetworkCallback()
    }

    private fun initYandexMetric(context: Context) {
        val config: YandexMetricaConfig =
            YandexMetricaConfig.newConfigBuilder(getYandexMetricKey())
                //TODO: 16.11.2021 включить после тестирования аналитики
                //.withStatisticsSending(!BuildConfig.DEBUG)
                .build()
        YandexMetrica.activate(context, config)
        YandexMetrica.enableActivityAutoTracking(context as Application)
    }

    private fun getYandexMetricKey() =
        if (BuildConfig.DEBUG) YANDEX_METRIC_DEBUG_KEY else YANDEX_METRIC_KEY

}