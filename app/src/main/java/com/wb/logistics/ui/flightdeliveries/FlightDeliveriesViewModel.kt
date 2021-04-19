package com.wb.logistics.ui.flightdeliveries

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import com.wb.logistics.ui.nav.domain.ScreenManager
import com.wb.logistics.ui.nav.domain.ScreenState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class FlightDeliveriesViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightDeliveriesResourceProvider,
    private val interactor: FlightDeliveriesInteractor,
    private val dataBuilder: FlightDeliveriesDataBuilder,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    val stateUIToolBar = MutableLiveData<String>()
    val stateUINav = MutableLiveData<FlightDeliveriesUINavState>()
    val stateUIList = MutableLiveData<FlightDeliveriesUIListState>()
    val stateUIBottom = MutableLiveData<FlightDeliveriesUIBottomState>()

    private var copyScannedBoxes = mutableListOf<ScannedBoxGroupByAddressEntity>()

    fun action(actionView: FlightDeliveriesUIAction) {
        when (actionView) {
            is FlightDeliveriesUIAction.GoToDeliveryClick -> {
                screenManager.saveScreenState(ScreenState.FLIGHT_DELIVERY)
                stateUINav.value = FlightDeliveriesUINavState.GoToDeliveryDialog
                stateUINav.value = FlightDeliveriesUINavState.Empty
            }
            is FlightDeliveriesUIAction.GoToDeliveryConfirmClick -> {
                stateUINav.value = FlightDeliveriesUINavState.NavigateToDelivery
            }
        }
    }

    fun update() {
        fetchFlightId()
        fetchScannedBoxGroupByAddress()
        fetchBottomState()
    }

    private fun fetchFlightId() {
        addSubscription(interactor.flightId()
            .subscribe({ fetchFlightIdComplete(it) }, { fetchFlightIdError(it) }))
    }

    private fun fetchFlightIdComplete(idFlight: Int) {
        if (screenManager.readScreenState() == ScreenState.FLIGHT_PICK_UP_POINT) {
            stateUIToolBar.value = resourceProvider.getFlightToolbar(idFlight)
        } else {
            stateUIToolBar.value = resourceProvider.getDeliveryToolbar(idFlight)
        }
    }

    private fun fetchFlightIdError(throwable: Throwable) {
        stateUIToolBar.value = resourceProvider.getEmpty()
    }


    private fun fetchScannedBoxGroupByAddress() {
        addSubscription(interactor.getScannedBoxesGroupByAddress()
            .doOnSuccess { copyScannedBoxes = it.toMutableList() }
            .flatMap { boxes ->
                Single.zip(build(boxes), count(boxes), { t1, t2 -> Pair(t1, t2) })
            }
            .map {
                FlightDeliveriesUIListState.ShowFlight(it.first,
                    resourceProvider.getCountBox(it.second))
            }
            .subscribe({ fetchScannedBoxGroupByAddressComplete(it) },
                { fetchScannedBoxGroupByAddressError(it) }))
    }

    private fun build(boxes: List<ScannedBoxGroupByAddressEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<ScannedBoxGroupByAddressEntity> ->
                val isEnabled = screenManager.readScreenState() == ScreenState.FLIGHT_DELIVERY
                dataBuilder.buildSuccessItem(item, isEnabled, index)
            }
            .toList()

    private fun count(boxes: List<ScannedBoxGroupByAddressEntity>) =
        Observable.fromIterable(boxes).map { it.count }.scan { v1, v2 -> v1 + v2 }.last(0)

    private fun fetchBottomState() {
        if (screenManager.readScreenState() == ScreenState.FLIGHT_PICK_UP_POINT) {
            stateUIBottom.value = FlightDeliveriesUIBottomState.GoToDelivery
        } else {
            stateUIBottom.value = FlightDeliveriesUIBottomState.Empty
        }
    }

    private fun fetchScannedBoxGroupByAddressComplete(flight: FlightDeliveriesUIListState) {
        stateUIList.value = flight
    }

    private fun fetchScannedBoxGroupByAddressError(throwable: Throwable) {
        stateUIList.value = when (throwable) {
            is UnauthorizedException -> FlightDeliveriesUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message)),
                resourceProvider.getCountBox(0)
            )
            else -> FlightDeliveriesUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorItem()),
                resourceProvider.getCountBox(0)
            )
        }
    }

    fun onItemClicked(idItem: Int) {
        // TODO: 19.04.2021 реализовать
        val item = copyScannedBoxes[idItem]
        stateUINav.value =
            FlightDeliveriesUINavState.NavigateToUpload("Далее выгрузка по адресу ${item.dstFullAddress} в количестве ${item.count} коробок")
    }

}