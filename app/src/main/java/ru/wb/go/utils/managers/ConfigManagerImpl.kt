package ru.wb.go.utils.managers

import ru.wb.go.app.AppPreffsKeys.APP_SERVER_KEY
import ru.wb.go.app.AppPreffsKeys.APP_VERSION_KEY
import ru.wb.go.app.AppPreffsKeys.AUTH_SERVER_KEY
import ru.wb.go.ui.config.data.ConfigDao
import ru.wb.go.ui.config.data.KeyValueDao
import ru.wb.go.utils.prefs.SharedWorker
import ru.wb.go.utils.reader.ConfigReader

class ConfigManagerImpl(private val reader: ConfigReader, private val worker: SharedWorker) :
    ConfigManager {

    private val params: ConfigDao
        get() = reader.build()

    override val authServersUrl: List<KeyValueDao>
        get() = params.authServers

    override fun saveAuthServerUrl(apiServer: KeyValueDao) {
        worker.save(AUTH_SERVER_KEY, apiServer)
    }

    override fun readDaoAuthServerUrl(): KeyValueDao {
        return worker.load(AUTH_SERVER_KEY, KeyValueDao::class.java) ?: KeyValueDao("", "")
    }

    override fun readAuthServerUrl(): String {
        return serverUrl(readDaoAuthServerUrl())
    }

    override val appServersUrl: List<KeyValueDao>
        get() = params.appServers

    override fun saveAppServerUrl(apiServer: KeyValueDao) {
        worker.save(APP_SERVER_KEY, apiServer)
    }

    override fun readDaoAppServerUrl(): KeyValueDao {
        return worker.load(APP_SERVER_KEY, KeyValueDao::class.java) ?: KeyValueDao("", "")
    }

    override fun readAppServerUrl(): String {
        return serverUrl(readDaoAppServerUrl())
    }

    override fun saveAppVersion(version: String) {
        worker.save(APP_VERSION_KEY, version)
    }

    override fun readAppVersion(): String {
        return worker.load(APP_VERSION_KEY, "")
    }

    private fun serverUrl(keyValueDao: KeyValueDao?): String {
        return keyValueDao?.value ?: ""
    }

    private fun saveIfNotExists() {
        if (!worker.isAllExists(AUTH_SERVER_KEY, APP_SERVER_KEY)) {
            saveAuthServerUrl(authServersUrl[0])
            saveAppServerUrl(appServersUrl[0])
        }
    }

    init {
        saveIfNotExists()
    }

}