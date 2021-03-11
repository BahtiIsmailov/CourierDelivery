package com.wb.logistics.utils.managers

import com.wb.logistics.app.AppPreffsKeys.AUTH_SERVER_KEY
import com.wb.logistics.ui.config.dao.ConfigDAO
import com.wb.logistics.ui.config.dao.KeyValueDAO
import com.wb.logistics.utils.prefs.SharedWorker
import com.wb.logistics.utils.reader.ConfigReader

class ConfigManagerImpl(private val reader: ConfigReader, private val worker: SharedWorker) :
    ConfigManager {

    private val params: ConfigDAO
        get() = reader.build()

    override val authServersUrl: List<KeyValueDAO>
        get() = params.authServers!!

    override fun saveAuthServerUrl(apiServer: KeyValueDAO) {
        worker.save(AUTH_SERVER_KEY, apiServer)
    }

    override fun readDaoAuthServerUrl(): KeyValueDAO? {
        return worker.load(AUTH_SERVER_KEY, KeyValueDAO::class.java)
    }

    override fun readAuthServerUrl(): String {
        return serverUrl(readDaoAuthServerUrl())
    }

    private fun serverUrl(keyValueDAO: KeyValueDAO?): String {
        return keyValueDAO?.value ?: ""
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