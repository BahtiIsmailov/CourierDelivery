package com.wb.logistics.ui.flightsloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightserror.FlightsErrorParameters
import com.wb.logistics.ui.flightsloader.domain.FlightDefinitionAction
import com.wb.logistics.ui.flightsloader.domain.FlightsLoaderInteractor
import com.wb.logistics.utils.LogUtils
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
        addSubscription(
            interactor.updateFlight().subscribe(
                { navigateToOnComplete(it) },
                { navigateToOnError(it) })
        )
    }

    private fun navigateToOnComplete(definitionAction: FlightDefinitionAction) {
        when (definitionAction) {
            FlightDefinitionAction.FlightEmpty -> {
                _countFlightsState.value = CountFlights(flightLoaderProvider.getEmptyFlight())
                _navState.value = NavigateTo(emptyDirections())
            }
            is FlightDefinitionAction.NavigateComplete -> {
                _countFlightsState.value = CountFlights(flightLoaderProvider.getOneFlight())
                _navState.value = NavigateTo(definitionAction.navDirections)
            }
        }
    }

    private fun navigateToOnError(throwable: Throwable) {
        LogUtils { logDebugApp(this.javaClass.name + " navigateToOnError " + throwable.toString()) }
        _countFlightsState.value = CountFlights(flightLoaderProvider.getEmptyFlight())
        _navState.value = NavigateTo(errorDirections(throwable.toString()))
    }

    private fun errorDirections(message: String) =
        FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsErrorFragment(
            FlightsErrorParameters(message))

    private fun emptyDirections() =
        FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsEmptyFragment()


    data class NavigateTo(val navDirections: NavDirections)
    data class CountFlights(val countFlights: String)

}