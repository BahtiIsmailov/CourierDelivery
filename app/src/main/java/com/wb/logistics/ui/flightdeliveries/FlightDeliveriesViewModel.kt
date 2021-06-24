package com.wb.logistics.ui.flightdeliveries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class FlightDeliveriesViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightDeliveriesResourceProvider,
    private val interactor: FlightDeliveriesInteractor,
    private val dataBuilder: FlightDeliveriesDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUIToolBar = MutableLiveData<FlightDeliveriesUIToolbarState>()
    val stateUIToolBar: LiveData<FlightDeliveriesUIToolbarState>
        get() = _stateUIToolBar

    private val _stateUINav = SingleLiveEvent<FlightDeliveriesUINavState>()
    val stateUINav: LiveData<FlightDeliveriesUINavState>
        get() = _stateUINav

    val bottomProgressEvent = MutableLiveData<Boolean>()

    val stateUIList = MutableLiveData<FlightDeliveriesUIListState>()

    private var copyScannedBoxes = mutableListOf<AttachedBoxGroupByOfficeEntity>()

    init {
        addSubscription(interactor.updatePvzAttachedBoxes().subscribe({}, {}))
    }

    fun update() {
        fetchFlightId()
        fetchAttachedBoxesGroupByOfficeId()
    }

    private fun fetchFlightId() {
        addSubscription(interactor.flightId()
            .map { resourceProvider.getDeliveryToolbar(it) }
            .subscribe({ fetchFlightIdComplete(it) }, { fetchFlightIdError() }))
    }

    private fun fetchFlightIdComplete(title: String) {
        _stateUIToolBar.value = FlightDeliveriesUIToolbarState.Delivery(title)
    }

    private fun fetchFlightIdError() {
        _stateUIToolBar.value =
            FlightDeliveriesUIToolbarState.Delivery(resourceProvider.getDeliveryToolbarEmpty())
    }

    private fun fetchAttachedBoxesGroupByOfficeId() {
        addSubscription(interactor.getAttachedBoxesGroupByOffice()
            .doOnSuccess { copyScannedBoxes = it.toMutableList() }
            .flatMap { boxes ->
                Single.zip(build(boxes),
                    isComplete(boxes),
                    { build, isComplete ->
                        FlightDeliveriesUIListState.ShowFlight(build, isComplete)
                    })
            }
            .subscribe({ fetchScannedBoxGroupByAddressComplete(it) },
                { fetchScannedBoxGroupByAddressError(it) }))
    }

    private fun build(boxes: List<AttachedBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<AttachedBoxGroupByOfficeEntity> ->
                dataBuilder.buildSuccessItem(index, item)
            }
            .toList()

    private fun isComplete(boxes: List<AttachedBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes)
            .filter { it.isUnloading }
            .map { it.isUnloading }
            .defaultIfEmpty(false)
            .firstOrError()

    private fun fetchScannedBoxGroupByAddressComplete(flight: FlightDeliveriesUIListState) {
        stateUIList.value = flight
    }

    private fun fetchScannedBoxGroupByAddressError(throwable: Throwable) {
        stateUIList.value = when (throwable) {
            is UnauthorizedException -> FlightDeliveriesUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message)), false)
            else -> FlightDeliveriesUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorItem()), false)
        }
    }

    fun onItemClicked(itemId: Int) {
        val item = copyScannedBoxes[itemId]
        if (item.isUnloading) {
            _stateUINav.value =
                FlightDeliveriesUINavState.NavigateToUnloadDetails(item.officeId, item.officeName)
        } else {
            _stateUINav.value = FlightDeliveriesUINavState.NavigateToUpload(item.officeId)
        }
    }

    fun onCompleteClick() {
        addSubscription(interactor.getAttachedBoxes().subscribe({
            _stateUINav.value =
                FlightDeliveriesUINavState.NavigateToDialogComplete(resourceProvider.getDescriptionDialog(
                    it))
        },
            {}))
    }

    fun onCompleteConfirm() {
        bottomProgressEvent.value = true
        addSubscription(interactor.switchScreenDcUnloading().subscribe(
            {
                _stateUINav.value = FlightDeliveriesUINavState.NavigateToCongratulation
            },
            {
                // TODO: 31.05.2021 реализовать сообщение
                bottomProgressEvent.value = false
            }))

    }

}