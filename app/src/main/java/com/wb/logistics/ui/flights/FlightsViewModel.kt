package com.wb.logistics.ui.flights

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.flights.domain.FlightEntity
import com.wb.logistics.ui.flights.domain.FlightsInteractor
import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable

class FlightsViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightResourceProvider,
    private val interactor: FlightsInteractor,
    private val dataBuilder: FlightsDataBuilder,
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
            FlightsUIAction.RemoveBoxesClick -> interactor.removeBoxes()
        }
    }

    init {
        fetchFlights()
        fetchBoxes()
    }

    private fun fetchBoxes() {
        addSubscription(interactor.changeBoxes().subscribe({ changeBoxesComplete(it) },
            { changeBoxesError(it) }))
    }

    private fun changeBoxesComplete(boxes: List<ReceptionBoxEntity>) {
        stateUIBottom.value =
            if (boxes.isEmpty()) FlightsUIBottomState.ScanBox
            else FlightsUIBottomState.ReturnBox
    }

    private fun changeBoxesError(error: Throwable) {

    }

    private fun fetchFlights() {
        stateUIList.value = FlightsUIListState.ProgressFlight(
            listOf(dataBuilder.buildProgressItem()),
            zeroFlight()
        )
        interactor.action.onNext(true)
        addSubscription(
            interactor.flight()
                .map {
                    when (it) {
                        is FlightEntity.Empty -> FlightsUIListState.UpdateFlight(
                            listOf(dataBuilder.buildEmptyItem()), zeroFlight()
                        )
                        is FlightEntity.Success -> FlightsUIListState.ShowFlight(
                            listOf(dataBuilder.buildSuccessItem(it)),
                            resourceProvider.getOneFlight()
                        )
                    }
                }
                .subscribe({ flightsComplete(it) }, { flightsError(it) })
        )
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