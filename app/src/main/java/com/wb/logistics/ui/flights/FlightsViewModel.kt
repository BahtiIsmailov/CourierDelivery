package com.wb.logistics.ui.flights

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.flights.delegates.items.FlightProgressItem
import com.wb.logistics.ui.flights.delegates.items.FlightRefreshItem
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.ui.res.AppResourceProvider
import io.reactivex.disposables.CompositeDisposable

class FlightsViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: AppResourceProvider,
    private val interactor: FlightsInteractor,
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<FlightsPasswordUIState>()

    fun action(actionView: FlightsPasswordUIAction) {
        when (actionView) {
            is FlightsPasswordUIAction.Refresh -> fetchFlights()
            is FlightsPasswordUIAction.NetworkInfoClick ->
                FlightsPasswordUIState.NavigateToNetworkInfoDialog
            is FlightsPasswordUIAction.ReceptionBoxesClick -> {
                FlightsPasswordUIState.NavigateToReceptionBox
            }
            is FlightsPasswordUIAction.ReturnToBalanceClick -> {
                FlightsPasswordUIState.NavigateToReturnBalanceDialog
            }
            is FlightsPasswordUIAction.ContinueAcceptanceClick ->
                FlightsPasswordUIState.NavigateToReceptionBox
        }
    }

    init {
        fetchFlights()
    }

    private fun fetchFlights() {
        stateUI.value = FlightsPasswordUIState.ProgressFlight(getProgressFlights())
        addSubscription(
            interactor.flight().subscribe({ flightsComplete(it) }, { flightsError(it) })
        )
    }

    private fun flightsComplete(flight: List<BaseItem>) {
        stateUI.value = if (flight.isEmpty()) FlightsPasswordUIState.UpdateFlight(getEmptyFlights())
        else FlightsPasswordUIState.ShowFlight(flight)
    }

    private fun flightsError(throwable: Throwable) {
        stateUI.value = FlightsPasswordUIState.UpdateFlight(getErrorFlights())
    }

    private fun getProgressFlights(): List<BaseItem> {
        val data = mutableListOf<BaseItem>()
        data.add(FlightProgressItem())
        return data
    }

    private fun getErrorFlights(): List<BaseItem> {
        val data = mutableListOf<BaseItem>()
        data.add(FlightRefreshItem(resourceProvider.getErrorFlight()))
        return data
    }

    private fun getEmptyFlights(): List<BaseItem> {
        val data = mutableListOf<BaseItem>()
        data.add(FlightRefreshItem(resourceProvider.getEmptyFlight()))
        return data
    }

}