package ru.wb.go.di.module

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.wb.go.app.AppConfig
import ru.wb.go.utils.managers.*
import ru.wb.go.utils.prefs.SharedWorker
import ru.wb.go.utils.prefs.SharedWorkerImpl
import ru.wb.go.utils.reader.ConfigReader
import ru.wb.go.utils.reader.ConfigReaderImpl
import ru.wb.go.utils.time.TimeFormatter
import ru.wb.go.utils.time.TimeFormatterImpl

const val PATH_CONFIG_NAMED = "pathConfig"

val utilsModule = module {

    fun provideSharedWorker(application: Application, gson: Gson): SharedWorker {
        return SharedWorkerImpl(application, gson)
    }

    fun provideDeviceManager(application: Application, worker: SharedWorker): DeviceManager {
        return DeviceManagerImpl(application, worker)
    }

    fun provideConfigPath(): String {
        return AppConfig.PATH_CONFIG
    }

    fun provideConfigReader(application: Application, gson: Gson, filePath: String): ConfigReader {
        return ConfigReaderImpl(application, gson, filePath)
    }

    fun provideConfigManager(
        configReader: ConfigReader,
        sharedWorker: SharedWorker,
    ): ConfigManager {
        return ConfigManagerImpl(configReader, sharedWorker)
    }

    fun provideTimeFormatter(): TimeFormatter {
        return TimeFormatterImpl()
    }

    fun provideTimeManager(worker: SharedWorker, timeFormatter: TimeFormatter): TimeManager {
        return TimeManagerImpl(worker, timeFormatter)
    }

//    fun provideYandexMetricManager(
//        deviceManager: DeviceManager,
//        tokenManager: TokenManager,
//        timeManager: TimeManager
//    ): YandexMetricManager {
//        return YandexMetricManagerImpl(deviceManager, tokenManager, timeManager)
//    }

    fun provideErrorManager(context: Context): ErrorDialogManager {
        return ErrorDialogManagerImpl(context)
    }

    fun provideSettingsManager(sharedWorker: SharedWorker): SettingsManager {
        return SettingsManagerImpl(sharedWorker)
    }

    fun providePlayManager(context: Context, settingsManager: SettingsManager): PlayManager {
        return PlayManagerImpl(context, settingsManager)
    }

    single { provideSharedWorker(get(), get()) }
    single { provideDeviceManager(get(), get()) }
    single(named(PATH_CONFIG_NAMED)) { provideConfigPath() }
    single { provideConfigReader(get(), get(), get(named(PATH_CONFIG_NAMED))) }
    single { provideConfigManager(get(), get()) }
    single { provideTimeFormatter() }
    single { provideTimeManager(get(), get()) }
    //single { provideYandexMetricManager(get(), get(), get()) }
    single { provideErrorManager(get()) }
    single { provideSettingsManager(get()) }
    single { providePlayManager(get(), get()) }
}