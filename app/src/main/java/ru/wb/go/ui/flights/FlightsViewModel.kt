package ru.wb.go.ui.flights

import androidx.lifecycle.MutableLiveData
import ru.wb.go.db.Optional
import ru.wb.go.network.exceptions.UnauthorizedException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.flights.domain.FlightsInteractor
import io.reactivex.disposables.CompositeDisposable

class FlightsViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: FlightsInteractor,
    private val dataBuilder: FlightsDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = SingleLiveEvent<FlightsUINavState>()
    val stateUIList = MutableLiveData<FlightsUIListState>()
    val stateUIBottom = MutableLiveData<FlightsUIBottomState>()

    fun action(actionView: FlightsUIAction) {
        when (actionView) {
            is FlightsUIAction.Refresh -> fetchFlights()
            is FlightsUIAction.NetworkInfoClick ->
                FlightsUINavState.NavigateToNetworkInfoDialog
            is FlightsUIAction.ReceptionBoxesClick ->
                stateUINav.value = FlightsUINavState.NavigateToReceptionBox
            is FlightsUIAction.ReturnToBalanceClick ->
                FlightsUINavState.NavigateToReturnBalanceDialog
            is FlightsUIAction.ContinueAcceptanceClick ->
                FlightsUINavState.NavigateToReceptionBox
        }
    }

    fun update() {
        fetchFlights()
    }

    init {
        observeFlightBoxesScanned()
        observeFlight()
    }

    private fun fetchFlights() {
        stateUIList.value = FlightsUIListState.ProgressFlight(
            listOf(dataBuilder.buildProgressItem())
        )
    }

    private fun observeFlightBoxesScanned() {
        addSubscription(interactor.observeFlightBoxScanned()
            .subscribe({ observeFlightBoxesComplete(it) },
                { observeFlightBoxesError(it) }))
    }

    private fun observeFlightBoxesComplete(boxes: Int) {
        stateUIBottom.value =
            if (boxes == 0) FlightsUIBottomState.ScanBox
            else FlightsUIBottomState.ReturnBox
    }

    private fun observeFlight() {
        addSubscription(interactor.observeFlightData()
            .map {
                when (it) {
                    is Optional.Empty -> FlightsUIListState.UpdateFlight(
                        listOf(dataBuilder.buildEmptyItem())
                    )
                    is Optional.Success -> FlightsUIListState.ShowFlight(
                        listOf(dataBuilder.buildSuccessItem(it))
                    )
                }
            }
            .subscribe({ flightsComplete(it) }, { flightsError(it) }))
    }

    private fun observeFlightBoxesError(error: Throwable) {
    }

    private fun flightsComplete(flight: FlightsUIListState) {
        stateUIList.value = flight
    }

    private fun flightsError(throwable: Throwable) {
        stateUIList.value = when (throwable) {
            is UnauthorizedException -> FlightsUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message))
            )
            else -> FlightsUIListState.UpdateFlight(listOf(dataBuilder.buildErrorItem()))
        }
    }

}