package com.wb.logistics.ui.flightdeliveriesdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractor
import io.reactivex.disposables.CompositeDisposable

class FlightDeliveriesDetailsViewModel(
    private val parameters: FlightDeliveriesDetailsParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: FlightDeliveriesDetailsResourceProvider,
    private val interactor: FlightDeliveriesDetailsInteractor,
    private val dataBuilder: FlightDeliveriesDetailsDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _itemsState = MutableLiveData<FlightDeliveriesDetailsItemsState>()
    val itemsState: LiveData<FlightDeliveriesDetailsItemsState>
        get() = _itemsState

    private val _stateUINav = SingleLiveEvent<FlightDeliveriesDetailsUINavState>()
    val stateUINav: LiveData<FlightDeliveriesDetailsUINavState>
        get() = _stateUINav

    init {
        _toolbarLabelState.value = Label(parameters.shortAddress)
        addSubscription(interactor.getUnloadedAndReturnBoxesGroupByOffice(parameters.currentOfficeId)
            .map { dataBuilder.buildItem(it) }
            .subscribe(
                { _itemsState.value = FlightDeliveriesDetailsItemsState.Items(it) },
                {}))
    }

    fun onCompleteClick() {
        _stateUINav.value =
            FlightDeliveriesDetailsUINavState.NavigateToUpload(parameters.currentOfficeId)
    }

    data class Label(val label: String)

}