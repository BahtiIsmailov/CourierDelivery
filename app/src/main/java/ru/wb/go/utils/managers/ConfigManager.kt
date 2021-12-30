package ru.wb.go.utils.managers

import ru.wb.go.ui.config.data.KeyValueDao

interface ConfigManager {
    val authServersUrl: List<KeyValueDao>
    fun saveAuthServerUrl(apiServer: KeyValueDao)
    fun readDaoAuthServerUrl(): KeyValueDao
    fun readAuthServerUrl(): String
    val appServersUrl: List<KeyValueDao>
    fun saveAppServerUrl(apiServer: KeyValueDao)
    fun readDaoAppServerUrl(): KeyValueDao
    fun readAppServerUrl(): String

    fun saveAppVersion(version: String)
    fun readAppVersion(): String
}