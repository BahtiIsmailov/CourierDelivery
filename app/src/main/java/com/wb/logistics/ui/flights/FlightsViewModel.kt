package com.wb.logistics.ui.flights

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable

class FlightsViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightResourceProvider,
    private val interactor: FlightsInteractor,
    private val dataBuilder: FlightsDataBuilder,
    //private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<FlightsUINavState>()
    val stateUIList = MutableLiveData<FlightsUIListState>()
    val stateUIBottom = MutableLiveData<FlightsUIBottomState>()

    fun action(actionView: FlightsUIAction) {
        when (actionView) {
            is FlightsUIAction.Refresh -> fetchFlights()
            is FlightsUIAction.NetworkInfoClick ->
                FlightsUINavState.NavigateToNetworkInfoDialog
            is FlightsUIAction.ReceptionBoxesClick -> {
                stateUINav.value = FlightsUINavState.NavigateToReceptionBox
                stateUINav.value = FlightsUINavState.Empty
            }
            is FlightsUIAction.ReturnToBalanceClick -> {
                FlightsUINavState.NavigateToReturnBalanceDialog
            }
            is FlightsUIAction.ContinueAcceptanceClick ->
                FlightsUINavState.NavigateToReceptionBox
            FlightsUIAction.RemoveBoxesClick ->
                addSubscription(interactor.deleteFlightBoxes()
                    .subscribe({ removeFlightBoxesComplete() }, { removeFlightBoxesError(it) }))
        }
    }

    private fun removeFlightBoxesComplete() {
        // TODO: 09.04.2021 реализовать
    }

    private fun removeFlightBoxesError(throwable: Throwable) {
        // TODO: 09.04.2021 реализовать
    }

    init {
        fetchFlights()
        observeFlightBoxesScanned()
        observeFlight()
    }

    private fun fetchFlights() {
        stateUIList.value = FlightsUIListState.ProgressFlight(
            listOf(dataBuilder.buildProgressItem()),
            zeroFlight()
        )
        interactor.updateFlight()
        addSubscription(interactor.flight().subscribe({ }, { flightsError(it) }))
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
        addSubscription(interactor.observeFlight()
            .map {
                when (it) {
                    is SuccessOrEmptyData.Empty -> FlightsUIListState.UpdateFlight(
                        listOf(dataBuilder.buildEmptyItem()), zeroFlight()
                    )
                    is SuccessOrEmptyData.Success -> FlightsUIListState.ShowFlight(
                        listOf(dataBuilder.buildSuccessItem(it)),
                        resourceProvider.getOneFlight()
                    )
                }
            }
            .subscribe({ flightsComplete(it) }, { flightsError(it) }))
    }

    private fun observeFlightBoxesError(error: Throwable) {
        LogUtils { logDebugApp(error.toString()) }
    }

    private fun flightsComplete(flight: FlightsUIListState) {
        stateUIList.value = flight
    }

    private fun flightsError(throwable: Throwable) {
        LogUtils { logDebugApp(throwable.toString()) }
        stateUIList.value = when (throwable) {
            is UnauthorizedException -> FlightsUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message)),
                zeroFlight()
            )
            else -> FlightsUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorItem()),
                zeroFlight()
            )
        }
    }

    private fun zeroFlight() = resourceProvider.getZeroFlight()

}