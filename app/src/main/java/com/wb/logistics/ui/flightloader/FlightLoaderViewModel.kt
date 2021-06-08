package com.wb.logistics.ui.flightloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightloader.domain.FlightsLoaderInteractor
import io.reactivex.disposables.CompositeDisposable

class FlightLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: FlightsLoaderInteractor,
    private val flightLoaderProvider: FlightLoaderProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navState = SingleLiveEvent<NavigateTo>()
    val navState: LiveData<NavigateTo>
        get() = _navState

    private val _navHeader = MutableLiveData<UserInfoEntity>()
    val navHeader: LiveData<UserInfoEntity>
        get() = _navHeader

    private val _countFlightsState = SingleLiveEvent<CountFlights>()
    val countFlightsState: LiveData<CountFlights>
        get() = _countFlightsState

    fun update() {
        toApp()
    }

    private fun toApp() {
        addSubscription(interactor.sessionInfo().subscribe({ _navHeader.value = it }, {}))
        addSubscription(interactor.navigateTo().subscribe(
            { navigateToOnComplete(it) },
            { navigateToOnError() })
        )
    }

    private fun navigateToOnComplete(navDirections: NavDirections) {
        _countFlightsState.value = CountFlights(flightLoaderProvider.getOneFlight())
        _navState.value = NavigateTo(navDirections)
    }

    private fun navigateToOnError() {
        _countFlightsState.value = CountFlights(flightLoaderProvider.getEmptyFlight())
        _navState.value = NavigateTo(
            FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsEmptyFragment())
    }

    data class NavigateTo(val navDirections: NavDirections)
    data class CountFlights(val countFlights: String)

}