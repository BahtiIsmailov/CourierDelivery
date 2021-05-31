package com.wb.logistics.ui.flightloader

import androidx.lifecycle.LiveData
import androidx.navigation.NavDirections
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightloader.domain.FlightsLoaderInteractor
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class FlightLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: FlightsLoaderInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navState = SingleLiveEvent<NavigateTo>()
    val navState: LiveData<NavigateTo>
        get() = _navState

    init {
        toApp()
    }

    private fun toApp() {
        addSubscription(interactor.updateFlight().subscribe(
            {
                when (it) {
                    is SuccessOrEmptyData.Empty -> {
                        _navState.value = NavigateTo(
                            FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsEmptyFragment())
                    }
                    is SuccessOrEmptyData.Success -> {
                        addSubscription(screenManager.loadState()
                            .subscribe({ _navState.value = NavigateTo(it) },
                                {}))
                    }
                }
            },
            {
                _navState.value = NavigateTo(
                    FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsEmptyFragment())
            }))
    }

    data class NavigateTo(val navDirections: NavDirections)

}