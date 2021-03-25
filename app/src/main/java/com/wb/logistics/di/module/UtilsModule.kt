package com.wb.logistics.di.module

import android.app.Application
import com.google.gson.Gson
import com.wb.logistics.app.AppConfig
import com.wb.logistics.utils.managers.ConfigManager
import com.wb.logistics.utils.managers.ConfigManagerImpl
import com.wb.logistics.utils.managers.DeviceManager
import com.wb.logistics.utils.managers.DeviceManagerImpl
import com.wb.logistics.utils.prefs.SharedWorker
import com.wb.logistics.utils.prefs.SharedWorkerImpl
import com.wb.logistics.utils.reader.ConfigReader
import com.wb.logistics.utils.reader.ConfigReaderImpl
import com.wb.logistics.utils.time.TimeFormatter
import com.wb.logistics.utils.time.TimeFormatterImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val PATH_CONFIG_NAMED = "pathConfig"

val utilsModule = module {

    fun provideSharedWorker(application: Application, gson: Gson): SharedWorker {
        return SharedWorkerImpl(application, gson)
    }

    fun provideDeviceManager(application: Application): DeviceManager {
        return DeviceManagerImpl(application)
    }

    fun provideConfigPath(): String {
        return AppConfig.PATH_CONFIG
    }

    fun provideConfigReader(application: Application, gson: Gson, filePath: String): ConfigReader {
        return ConfigReaderImpl(application, gson, filePath)
    }

    fun provideConfigManager(
        configReader: ConfigReader,
        sharedWorker: SharedWorker
    ): ConfigManager {
        return ConfigManagerImpl(configReader, sharedWorker)
    }

    fun provideTimeFormatter(): TimeFormatter {
        return TimeFormatterImpl()
    }

    single { provideSharedWorker(get(), get()) }
    single { provideDeviceManager(get()) }
    single(named(PATH_CONFIG_NAMED)) { provideConfigPath() }
    single { provideConfigReader(get(), get(), get(named(PATH_CONFIG_NAMED))) }
    single { provideConfigManager(get(), get()) }
    single { provideTimeFormatter() }


}