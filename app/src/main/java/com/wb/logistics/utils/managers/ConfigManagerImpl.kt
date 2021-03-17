package com.wb.logistics.utils.managers

import com.wb.logistics.app.AppPreffsKeys.AUTH_SERVER_KEY
import com.wb.logistics.ui.config.dao.ConfigDao
import com.wb.logistics.ui.config.dao.KeyValueDao
import com.wb.logistics.utils.prefs.SharedWorker
import com.wb.logistics.utils.reader.ConfigReader

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
        return worker.load(AUTH_SERVER_KEY, KeyValueDao::class.java) ?: authServersUrl.last()
    }

    override fun readAuthServerUrl(): String {
        return serverUrl(readDaoAuthServerUrl())
    }

    private fun serverUrl(keyValueDao: KeyValueDao?): String {
        return keyValueDao?.value ?: ""
    }

    private fun saveIfNotExists() {
        if (!worker.isAllExists(AUTH_SERVER_KEY)) {
            saveAuthServerUrl(authServersUrl[0])
        }
    }

    init {
        saveIfNotExists()
    }

}