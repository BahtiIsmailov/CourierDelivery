package ru.wb.go.ui.flightpickpoint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.exceptions.UnauthorizedException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.Label
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.flightpickpoint.domain.FlightPickPointInteractor
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class FlightPickPointViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightPickPointResourceProvider,
    private val interactor: FlightPickPointInteractor,
    private val dataBuilder: FlightPickPointDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUINav = SingleLiveEvent<FlightPickPointUINavState>()
    val stateUINav: LiveData<FlightPickPointUINavState>
        get() = _stateUINav

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _stateUI = SingleLiveEvent<FlightPickPointUIState>()
    val stateUI: LiveData<FlightPickPointUIState>
        get() = _stateUI

    val bottomProgressEvent = MutableLiveData<Boolean>()

    val stateUIList = MutableLiveData<FlightPickPointUIListState>()

    private var copyScannedBoxes = mutableListOf<PickupPointBoxGroupByOfficeEntity>()

    init {
        observeNetworkState()
        fetchFlightId()
        fetchAttachedBoxesGroupByOfficeId()
    }

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
        addSubscription(interactor.createTTN()
            .subscribe({ createTTNComplete() }, { crateTTNError(it) })
        )
    }

    private fun createTTNComplete() {
        _stateUINav.value = FlightPickPointUINavState.NavigateToDelivery
        bottomProgressEvent.value = false
    }

    private fun crateTTNError(throwable: Throwable) {
        _stateUI.value = FlightPickPointUIState.Error(when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> resourceProvider.getFlightListError()
            else -> resourceProvider.getGenericError()
        })

    }

    private fun fetchFlightId() {
        addSubscription(interactor.flightId()
            .subscribe({ flightIdComplete(it) }) { flightIdError() })
    }

    private fun flightIdComplete(flightId: Int) {
        _toolbarLabelState.value = Label(resourceProvider.getFlightToolbar(flightId))
    }

    private fun flightIdError() {
        _toolbarLabelState.value = Label(resourceProvider.getFlightNotDefineToolbar())
    }

    private fun fetchAttachedBoxesGroupByOfficeId() {
        addSubscription(interactor.getAttachedBoxesGroupByOffice()
            .doOnSuccess { copyScannedBoxes = it.toMutableList() }
            .flatMap { convertToFlightPickPointUIListState(it) }
            .subscribe({ fetchScannedBoxGroupByAddressComplete(it) })
            { fetchScannedBoxGroupByAddressError(it) })
    }

    private fun convertToFlightPickPointUIListState(boxes: List<PickupPointBoxGroupByOfficeEntity>) =
        Single.zip(buildItems(boxes), count(boxes),
            { items, countBox -> FlightPickPointUIListState.ShowFlight(items, countBox) })

    private fun buildItems(boxes: List<PickupPointBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<PickupPointBoxGroupByOfficeEntity> ->
                dataBuilder.buildSuccessItem(item, index)
            }
            .toList()

    private fun count(boxes: List<PickupPointBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes)
            .map { it.deliverCount }
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

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {}))
    }

}