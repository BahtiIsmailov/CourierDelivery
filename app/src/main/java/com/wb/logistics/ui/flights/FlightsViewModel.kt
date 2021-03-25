package com.wb.logistics.ui.flights

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.flights.domain.FlightEntity
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable

class FlightsViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightResourceProvider,
    private val interactor: FlightsInteractor,
    private val dataBuilder: FlightsDataBuilder
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
        stateUI.value = FlightsPasswordUIState.ProgressFlight(
            listOf(dataBuilder.buildProgressItem()),
            zeroFlight()
        )
        interactor.action.onNext(true)
        addSubscription(
            interactor.flight()
                .map {
                    when (it) {
                        is FlightEntity.Empty -> FlightsPasswordUIState.UpdateFlight(
                            listOf(dataBuilder.buildEmptyItem()), zeroFlight()
                        )
                        is FlightEntity.Success -> FlightsPasswordUIState.ShowFlight(
                            listOf(dataBuilder.buildSuccessItem(it)),
                            resourceProvider.getOneFlight()
                        )
                    }
                }
                .subscribe({ flightsComplete(it) }, { flightsError(it) })
        )
    }

    private fun flightsComplete(flight: FlightsPasswordUIState) {
        stateUI.value = flight
    }

    private fun flightsError(throwable: Throwable) {
        LogUtils { logDebugApp(throwable.toString()) }
        stateUI.value = when (throwable) {
            is UnauthorizedException -> FlightsPasswordUIState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message)),
                zeroFlight()
            )
            else -> FlightsPasswordUIState.UpdateFlight(
                listOf(dataBuilder.buildErrorItem()),
                zeroFlight()
            )
        }
    }

    private fun zeroFlight() = resourceProvider.getZeroFlight()

}