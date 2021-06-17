package com.wb.logistics.ui.flightpickpoint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightpickpoint.domain.FlightPickPointInteractor
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class FlightPickPointViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightPickPointResourceProvider,
    private val interactor: FlightPickPointInteractor,
    private val dataBuilder: FlightPickPointDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUIToolBar = MutableLiveData<FlightPickPointUIToolbarState>()
    val stateUIToolBar: LiveData<FlightPickPointUIToolbarState>
        get() = _stateUIToolBar

    private val _stateUINav = SingleLiveEvent<FlightPickPointUINavState>()
    val stateUINav: LiveData<FlightPickPointUINavState>
        get() = _stateUINav

    private val _stateUI = SingleLiveEvent<FlightPickPointUIState>()
    val stateUI: LiveData<FlightPickPointUIState>
        get() = _stateUI

    val bottomProgressEvent = MutableLiveData<Boolean>()

    val stateUIList = MutableLiveData<FlightPickPointUIListState>()

    private var copyScannedBoxes = mutableListOf<AttachedBoxGroupByOfficeEntity>()

    fun action(actionView: FlightPickPointUIAction) {
        when (actionView) {
            is FlightPickPointUIAction.GoToDeliveryClick -> showDeliveryDialog()
            is FlightPickPointUIAction.GoToDeliveryConfirmClick -> goToDelivery()
        }
    }

    private fun showDeliveryDialog() {
        _stateUINav.value = FlightPickPointUINavState.ShowDeliveryDialog
    }

    private fun goToDelivery() {
        bottomProgressEvent.value = true
        addSubscription(interactor.switchScreenToDelivery().subscribe(
            {
                _stateUINav.value = FlightPickPointUINavState.NavigateToDelivery
                bottomProgressEvent.value = false
            }) { switchScreenToDeliveryError(it) }
        )
    }

    private fun switchScreenToDeliveryError(throwable: Throwable) {
        _stateUI.value = FlightPickPointUIState.Error(when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> resourceProvider.getFlightListError()
            else -> resourceProvider.getGenericError()
        })

    }

    fun update() {
        fetchFlightId()
        fetchAttachedBoxesGroupByOfficeId()
    }

    private fun fetchFlightId() {
        addSubscription(interactor.flightId()
            .subscribe({ flightIdComplete(it) }) { flightIdError() })
    }

    private fun flightIdComplete(flightId: Int) {
        _stateUIToolBar.value =
            FlightPickPointUIToolbarState.Flight(resourceProvider.getFlightToolbar(flightId))
    }

    private fun flightIdError() {
        _stateUIToolBar.value =
            FlightPickPointUIToolbarState.Flight(resourceProvider.getFlightNotDefineToolbar())
    }

    private fun fetchAttachedBoxesGroupByOfficeId() {
        addSubscription(interactor.getAttachedBoxesGroupByOffice()
            .doOnSuccess { copyScannedBoxes = it.toMutableList() }
            .flatMap { convertToFlightPickPointUIListState(it) }
            .subscribe({ fetchScannedBoxGroupByAddressComplete(it) })
            { fetchScannedBoxGroupByAddressError(it) })
    }

    private fun convertToFlightPickPointUIListState(boxes: List<AttachedBoxGroupByOfficeEntity>) =
        Single.zip(buildItems(boxes), count(boxes),
            { items, countBox -> FlightPickPointUIListState.ShowFlight(items, countBox) })

    private fun buildItems(boxes: List<AttachedBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<AttachedBoxGroupByOfficeEntity> ->
                dataBuilder.buildSuccessItem(item, index)
            }
            .toList()

    private fun count(boxes: List<AttachedBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes)
            .map { it.attachedCount }
            .scan { v1, v2 -> v1 + v2 }
            .last(0)
            .map { resourceProvider.getCountBox(it) }

    private fun fetchScannedBoxGroupByAddressComplete(flight: FlightPickPointUIListState) {
        stateUIList.value = flight
    }

    private fun fetchScannedBoxGroupByAddressError(throwable: Throwable) {
        stateUIList.value = when (throwable) {
            is UnauthorizedException -> FlightPickPointUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message)),
                resourceProvider.getCountBox(0)
            )
            else -> FlightPickPointUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorItem()),
                resourceProvider.getCountBox(0)
            )
        }
    }

}