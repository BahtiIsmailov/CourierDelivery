package com.wb.logistics.ui.flightpickpoint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightpickpoint.domain.FlightPickPointInteractor
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class FlightPickPointViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightPickPointResourceProvider,
    private val interactor: FlightPickPointInteractor,
    private val dataBuilder: FlightPickPointDataBuilder,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUIToolBar = MutableLiveData<FlightPickPointUIToolbarState>()
    val stateUIToolBar: LiveData<FlightPickPointUIToolbarState>
        get() = _stateUIToolBar

    private val _stateUINav = SingleLiveEvent<FlightPickPointUINavState>()
    val stateUINav: LiveData<FlightPickPointUINavState>
        get() = _stateUINav

    val bottomProgressEvent = MutableLiveData<Boolean>()

    val stateUIList = MutableLiveData<FlightPickPointUIListState>()

    private var copyScannedBoxes = mutableListOf<AttachedBoxGroupByOfficeEntity>()

    fun action(actionView: FlightPickPointUIAction) {
        when (actionView) {
            is FlightPickPointUIAction.GoToDeliveryClick -> {
                _stateUINav.value = FlightPickPointUINavState.ShowDeliveryDialog
            }
            is FlightPickPointUIAction.GoToDeliveryConfirmClick -> {
                bottomProgressEvent.value = true
                addSubscription(interactor.switchScreen().subscribe({
                    _stateUINav.value = FlightPickPointUINavState.NavigateToDelivery
                    bottomProgressEvent.value = false
                }, {
                    // TODO: 30.05.2021 реализовать сообщение
                    bottomProgressEvent.value = false
                }))
            }
        }
    }

    fun update() {
        fetchFlightId()
        fetchAttachedBoxesGroupByOfficeId()
    }

    private fun fetchFlightId() {
        addSubscription(interactor.flightId()
            .subscribe({ fetchFlightIdComplete(it) }, { fetchFlightIdError(it) }))
    }

    private fun fetchFlightIdComplete(flightId: Int) {
        _stateUIToolBar.value = FlightPickPointUIToolbarState.Flight(resourceProvider.getFlightToolbar(flightId))
    }

    private fun fetchFlightIdError(throwable: Throwable) {

    }

    private fun fetchAttachedBoxesGroupByOfficeId() {
        addSubscription(interactor.getAttachedBoxesGroupByOffice()
            .doOnSuccess { copyScannedBoxes = it.toMutableList() }
            .flatMap { boxes ->
                Single.zip(build(boxes, true), count(boxes), { t1, t2 -> Pair(t1, t2) })
            }
            .map {
                FlightPickPointUIListState.ShowFlight(it.first,
                    resourceProvider.getCountBox(it.second))
            }
            .subscribe({ fetchScannedBoxGroupByAddressComplete(it) },
                { fetchScannedBoxGroupByAddressError(it) }))
    }

    private fun build(boxes: List<AttachedBoxGroupByOfficeEntity>, isEnabled: Boolean) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<AttachedBoxGroupByOfficeEntity> ->
                dataBuilder.buildSuccessItem(item, isEnabled, index)
            }
            .toList()

    private fun count(boxes: List<AttachedBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes)
            .map { it.attachedCount }
            .scan { v1, v2 -> v1 + v2 }.last(0)

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