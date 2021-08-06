package ru.wb.perevozka.di.module

import android.app.Application
import com.google.gson.Gson
import ru.wb.perevozka.app.AppConfig
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.*
import ru.wb.perevozka.utils.prefs.SharedWorker
import ru.wb.perevozka.utils.prefs.SharedWorkerImpl
import ru.wb.perevozka.utils.reader.ConfigReader
import ru.wb.perevozka.utils.reader.ConfigReaderImpl
import ru.wb.perevozka.utils.time.TimeFormatter
import ru.wb.perevozka.utils.time.TimeFormatterImpl
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

    fun provideScreenManager(
        worker: SharedWorker,
        rxSchedulerFactory: RxSchedulerFactory,
        appRemoteRepository: AppRemoteRepository,
        appLocalRepository: AppLocalRepository,
        timeManager: TimeManager,
    ): ScreenManager {
        return ScreenManagerImpl(worker,
            rxSchedulerFactory,
            appRemoteRepository,
            appLocalRepository,
            timeManager)
    }

    single { provideSharedWorker(get(), get()) }
    single { provideDeviceManager(get()) }
    single(named(PATH_CONFIG_NAMED)) { provideConfigPath() }
    single { provideConfigReader(get(), get(), get(named(PATH_CONFIG_NAMED))) }
    single { provideConfigManager(get(), get()) }
    single { provideTimeFormatter() }
    single { provideTimeManager(get(), get()) }
    single { provideScreenManager(get(), get(), get(), get(), get()) }

}