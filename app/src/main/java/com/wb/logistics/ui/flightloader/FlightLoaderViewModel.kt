package com.wb.logistics.ui.flightloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightloader.domain.FlightsLoaderInteractor
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class FlightLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: FlightsLoaderInteractor,
    private val screenManager: ScreenManager,
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
        addSubscription(interactor.updateFlight().subscribe(
            { flightData -> flightOnComplete(flightData) },
            { flightOnEmpty() })
        )
    }

    private fun flightOnComplete(flightData: SuccessOrEmptyData<FlightData>?) {
        when (flightData) {
            is SuccessOrEmptyData.Empty -> flightOnEmpty()
            is SuccessOrEmptyData.Success -> loadStatus(flightData)
        }
    }

    private fun flightOnEmpty() {
        _countFlightsState.value = CountFlights(flightLoaderProvider.getZeroFlight())
        _navState.value = NavigateTo(
            FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsEmptyFragment())
    }

    private fun loadStatus(flightData: SuccessOrEmptyData.Success<FlightData>) {
        _countFlightsState.value = CountFlights(flightLoaderProvider.getOneFlight())
        addSubscription(screenManager.loadStatus(flightData.data.flight.toString())
            .subscribe({ _navState.value = NavigateTo(it) }, {})
        )
    }

    data class NavigateTo(val navDirections: NavDirections)
    data class CountFlights(val countFlights: String)

}