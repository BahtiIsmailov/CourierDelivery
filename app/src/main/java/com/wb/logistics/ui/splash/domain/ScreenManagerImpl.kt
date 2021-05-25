package com.wb.logistics.ui.splash.domain

import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.utils.prefs.SharedWorker

class ScreenManagerImpl(private val worker: SharedWorker) : ScreenManager {

    // TODO: 29.04.2021 переработать решение на обобщениях

    override fun saveScreenState(screenManagerState: ScreenManagerState) {
        worker.save(AppPreffsKeys.SCREEN_ID_KEY, screenManagerState.id)
        if (screenManagerState is ScreenManagerState.Unloading)
            worker.save(AppPreffsKeys.SCREEN_UNLOADING_KEY, screenManagerState)
    }

    override fun readScreenState(): ScreenManagerState {
        val screenId: ScreenManagerState.ScreenId =
            worker.load(AppPreffsKeys.SCREEN_ID_KEY, ScreenManagerState.ScreenId::class.java)
                ?: return ScreenManagerState.Flight

        return when (screenId) {
            ScreenManagerState.ScreenId.FLIGHT -> ScreenManagerState.Flight
            ScreenManagerState.ScreenId.FLIGHT_DELIVERY -> ScreenManagerState.FlightDelivery
            ScreenManagerState.ScreenId.FLIGHT_PIK_UP_POINT -> ScreenManagerState.FlightPickUpPoint
            ScreenManagerState.ScreenId.RECEPTION_SCAN -> ScreenManagerState.ReceptionScan
            ScreenManagerState.ScreenId.UNLOADING -> worker.load(AppPreffsKeys.SCREEN_UNLOADING_KEY,
                ScreenManagerState.Unloading::class.java) ?: ScreenManagerState.Flight
            ScreenManagerState.ScreenId.DC_UNLOADING -> ScreenManagerState.DcUnloading
        }
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.SCREEN_ID_KEY)
    }

}