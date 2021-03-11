package com.wb.logistics.utils.managers

import com.wb.logistics.ui.config.dao.KeyValueDAO

interface ConfigManager {
    val authServersUrl: List<KeyValueDAO>
    fun saveAuthServerUrl(apiServer: KeyValueDAO)
    fun readDaoAuthServerUrl(): KeyValueDAO?
    fun readAuthServerUrl(): String
}