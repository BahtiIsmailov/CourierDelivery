package com.wb.logistics.ui.flights

import androidx.lifecycle.LiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.splash.domain.ScreenManager
import com.wb.logistics.ui.splash.domain.ScreenManagerState
import io.reactivex.disposables.CompositeDisposable

class FlightLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navState = SingleLiveEvent<FlightLoaderUINavState>()
    val navState: LiveData<FlightLoaderUINavState>
        get() = _navState

    init {
        toApp()
    }

    private fun toApp() {

        when (val state = screenManager.readScreenState()) {
            is ScreenManagerState.Flight -> _navState.value =
                FlightLoaderUINavState.NavigateToFlight
            is ScreenManagerState.ReceptionScan -> {
                _navState.value = FlightLoaderUINavState.NavigateToReceptionScan
            }
            is ScreenManagerState.FlightPickUpPoint -> _navState.value =
                FlightLoaderUINavState.NavigateToPickUpPoint
            is ScreenManagerState.FlightDelivery -> _navState.value =
                FlightLoaderUINavState.NavigateToDelivery
            is ScreenManagerState.Unloading ->
                _navState.value =
                    FlightLoaderUINavState.NavigateToUnloading(state.officeId, state.shortAddress)
        }
    }

}