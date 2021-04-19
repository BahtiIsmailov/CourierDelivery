package com.wb.logistics.ui.nav.domain

import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.utils.prefs.SharedWorker

class ScreenManagerImpl(private val worker: SharedWorker) : ScreenManager {

    override fun saveScreenState(screen: ScreenState) {
        worker.save(AppPreffsKeys.SCREEN_STATE_KEY, screen)
    }

    override fun readScreenState(): ScreenState {
        return worker.load(AppPreffsKeys.SCREEN_STATE_KEY, ScreenState::class.java) ?: ScreenState.FLIGHT
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.PHONE_KEY)
    }

}