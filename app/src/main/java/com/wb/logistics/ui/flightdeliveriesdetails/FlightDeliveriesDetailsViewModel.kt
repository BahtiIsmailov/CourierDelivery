package com.wb.logistics.ui.flightdeliveriesdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
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

    init {
        _toolbarLabelState.value = Label(parameters.shortAddress)
        addSubscription(interactor.getUnloadedAndReturnBoxesGroupByOffice(parameters.dstOfficeId)
            .map { dataBuilder.buildItem(it) }
            .subscribe(
                { _itemsState.value = FlightDeliveriesDetailsItemsState.Items(it) },
                {}))
    }

    data class Label(val label: String)

}