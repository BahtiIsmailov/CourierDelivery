package com.wb.logistics.ui.flightdeliveries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightdeliveries.domain.FlightDeliveriesInteractor
import com.wb.logistics.utils.LogUtils
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

    private val _stateUIProgress = SingleLiveEvent<FlightDeliveriesUIProgressState>()
    val stateUIProgress: LiveData<FlightDeliveriesUIProgressState>
        get() = _stateUIProgress

    val stateUIList = MutableLiveData<FlightDeliveriesUIListState>()

    private var copyScannedBoxes = mutableListOf<DeliveryBoxGroupByOfficeEntity>()

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    init {
        addSubscription(interactor.updatePvzAttachedBoxes().subscribe({}, {}))
    }

    fun update() {
        fetchFlightId()
        fetchDeliveryBoxesGroupByOfficeId()
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

    private fun fetchDeliveryBoxesGroupByOfficeId() {
        addSubscription(interactor.getDeliveryBoxesGroupByOffice()
            .doOnSuccess { copyScannedBoxes = it.toMutableList() }
            .flatMap { boxes ->
                LogUtils { logDebugApp("fetchDeliveryBoxesGroupByOfficeId " + boxes) }
                Single.zip(buildPvzItem(boxes), completeDeliveryState(boxes),
                    { build, completeDeliveryState ->
                        FlightDeliveriesUIListState.ShowFlight(build, completeDeliveryState)
                    })
            }
            .subscribe({ fetchScannedBoxGroupByAddressComplete(it) },
                { fetchScannedBoxGroupByAddressError(it) }))
    }

    private fun buildPvzItem(boxes: List<DeliveryBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map { (index, item): IndexedValue<DeliveryBoxGroupByOfficeEntity> ->
                dataBuilder.buildPvzSuccessItem(index, item)
            }
            .toList()

    private fun completeDeliveryState(boxes: List<DeliveryBoxGroupByOfficeEntity>) =
        Observable.fromIterable(boxes)
            .map { it.deliverCount }
            .reduce(0, { accumulator, attachedCount -> accumulator + attachedCount })
            .map {
                if (it == 0) FlightDeliveriesUIBottomState.ShowCompletePositiveDelivery
                else FlightDeliveriesUIBottomState.ShowCompleteNegativeDelivery
            }

    private fun fetchScannedBoxGroupByAddressComplete(flight: FlightDeliveriesUIListState) {
        stateUIList.value = flight
    }

    private fun fetchScannedBoxGroupByAddressError(throwable: Throwable) {
        stateUIList.value = when (throwable) {
            is UnauthorizedException -> FlightDeliveriesUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorMessageItem(throwable.message)),
                FlightDeliveriesUIBottomState.Empty)
            else -> FlightDeliveriesUIListState.UpdateFlight(
                listOf(dataBuilder.buildErrorItem()), FlightDeliveriesUIBottomState.Empty)
        }
    }

    fun onItemClicked(itemId: Int) {
        val item = copyScannedBoxes[itemId]
        _stateUINav.value = if (item.visitedAt.isEmpty()) {
             FlightDeliveriesUINavState.NavigateToUpload(item.officeId)
        } else FlightDeliveriesUINavState.NavigateToUnloadDetails(item.officeId, item.officeName)
    }

    fun onCompleteDeliveryNegativeClick() {
        addSubscription(interactor.getDeliveryBoxesGroupByOffice()
            .flatMap { boxes ->
                Observable.fromIterable(boxes)
                    .map { it.deliverCount }
                    .reduce(0, { accumulator, attachedCount -> accumulator + attachedCount })
            }
            .subscribe({
                _stateUINav.value =
                    FlightDeliveriesUINavState.NavigateToDialogComplete(
                        resourceProvider.getDescriptionDialog(it))
            }) { }
        )
    }

    fun onCompleteDeliveryPositiveClick() {
        _stateUIProgress.value = FlightDeliveriesUIProgressState.CompletePositiveDeliveryProgress
        toCongratulation()
    }

    fun onCompleteConfirm() {
        _stateUIProgress.value = FlightDeliveriesUIProgressState.CompleteNegativeDeliveryProgress
        toCongratulation()
    }

    private fun toCongratulation() {
        addSubscription(interactor.switchScreenToDcUnloading().subscribe(
            { switchScreenToDcUnloadingComplete() }) { switchScreenToDcUnloadingError(it) })
    }

    private fun switchScreenToDcUnloadingComplete() {
        _stateUIProgress.value = FlightDeliveriesUIProgressState.CompleteDeliveryNormal
        _stateUINav.value = FlightDeliveriesUINavState.NavigateToCongratulation
    }

    private fun switchScreenToDcUnloadingError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getCompleteDeliveryDialogMessage()
        }
        _stateUIProgress.value = FlightDeliveriesUIProgressState.CompleteDeliveryNormal
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getCompleteDeliveryDialogTitle(),
            message,
            resourceProvider.getCompleteDeliveryDialogButton())
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}