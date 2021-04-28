package com.wb.logistics.ui.flightdeliveries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByAddressEntity
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
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

    private val _stateUINav = SingleLiveEvent<FlightDeliveriesUINavState>()
    val stateUINav: LiveData<FlightDeliveriesUINavState>
        get() = _stateUINav

    val stateUIList = MutableLiveData<FlightDeliveriesUIListState>()
    val stateUIBottom = MutableLiveData<FlightDeliveriesUIBottomState>()

    private var copyScannedBoxes = mutableListOf<AttachedBoxGroupByAddressEntity>()

    init {
        screenManager.saveScreenState(ScreenState.FLIGHT_DELIVERY)
    }

    fun action(actionView: FlightDeliveriesUIAction) {
        when (actionView) {
            is FlightDeliveriesUIAction.GoToDeliveryClick -> {
                _stateUINav.value = FlightDeliveriesUINavState.GoToDeliveryDialog
                //_stateUINav.value = FlightDeliveriesUINavState.Empty
            }
            is FlightDeliveriesUIAction.GoToDeliveryConfirmClick -> {
                screenManager.saveScreenState(ScreenState.FLIGHT_DELIVERY)
                _stateUINav.value = FlightDeliveriesUINavState.NavigateToDelivery
            }
        }
    }

    fun update() {
        fetchFlightId()
        fetchAttachedBoxesGroupByAddress()
        fetchBottomState()
    }

    private fun fetchFlightId() {
        addSubscription(interactor.flightId()
            .subscribe({ fetchFlightIdComplete(it) }, { fetchFlightIdError(it) }))
    }

    private fun fetchFlightIdComplete(idFlight: Int) {
        stateUIToolBar.value =  if (screenManager.readScreenState() == ScreenState.FLIGHT_PICK_UP_POINT) {
            resourceProvider.getFlightToolbar(idFlight)
        } else {
            resourceProvider.getDeliveryToolbar(idFlight)
        }
    }

    private fun fetchFlightIdError(throwable: Throwable) {
        stateUIToolBar.value = resourceProvider.getEmpty()
    }


    private fun fetchAttachedBoxesGroupByAddress() {
        addSubscription(interactor.getAttachedBoxesGroupByAddress()
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

    private fun build(boxes: List<AttachedBoxGroupByAddressEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<AttachedBoxGroupByAddressEntity> ->
                val isEnabled = screenManager.readScreenState() == ScreenState.FLIGHT_DELIVERY
                dataBuilder.buildSuccessItem(item, isEnabled, index)
            }
            .toList()

    private fun count(boxes: List<AttachedBoxGroupByAddressEntity>) =
        Observable.fromIterable(boxes).map { it.undoCount }.scan { v1, v2 -> v1 + v2 }.last(0)

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

    fun onItemClicked(itemId: Int) {
        val item = copyScannedBoxes[itemId]
        _stateUINav.value =
            FlightDeliveriesUINavState.NavigateToUpload(item.officeId, item.officeName)
    }

}