package ru.wb.go.utils.managers

interface SettingsManager {
    fun resetSettings()
    fun loadFlash(): Boolean
    fun saveFlash(state: Boolean)
}