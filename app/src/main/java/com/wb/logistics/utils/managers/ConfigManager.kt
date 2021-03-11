package com.wb.logistics.utils.managers

import com.wb.logistics.ui.config.dao.KeyValueDao

interface ConfigManager {
    val authServersUrl: List<KeyValueDao>
    fun saveAuthServerUrl(apiServer: KeyValueDao)
    fun readDaoAuthServerUrl(): KeyValueDao?
    fun readAuthServerUrl(): String
}