package ru.wb.go.utils.managers

import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.utils.prefs.SharedWorker

class SettingsManagerImpl(private val worker: SharedWorker) : SettingsManager {
    override fun resetSettings() {
        worker.delete(AppPreffsKeys.SETTING_START_FLASH_ON)
        worker.delete(AppPreffsKeys.SETTING_VOICE_SCAN)
    }

    override fun getSetting(name: String, default: Boolean): Boolean {
        return worker.load(name, default)
    }

    override fun setSetting(name: String, state: Boolean) {
        worker.save(name, state)
    }

    override fun checkNewInstall(appVersion: String): Boolean {
        val check = worker.load(AppPreffsKeys.NEW_INSTALLATION, "")

        if (check == appVersion){
            return false
        }

        worker.save(AppPreffsKeys.NEW_INSTALLATION, appVersion)
        return true
    }
}