package ru.wb.go.utils.managers

import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.utils.prefs.SharedWorker

class SettingsManagerImpl(private val worker: SharedWorker):SettingsManager {
    override fun resetSettings() {
        worker.delete(AppPreffsKeys.START_FLASH_ON)
    }

    override fun loadFlash(): Boolean {
        return worker.load(AppPreffsKeys.START_FLASH_ON, false)
    }

    override fun saveFlash(state: Boolean) {
        worker.save(AppPreffsKeys.START_FLASH_ON, state)
    }
}