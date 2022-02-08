package ru.wb.go.utils.managers

interface SettingsManager {
    fun resetSettings()
    fun getSetting(name: String, default: Boolean): Boolean
    fun setSetting(name: String, state: Boolean)

    fun checkNewInstall(appVersion: String): Boolean
}