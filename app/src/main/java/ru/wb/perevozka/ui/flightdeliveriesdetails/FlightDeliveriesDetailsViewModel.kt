package ru.wb.perevozka.ui.flightdeliveriesdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.flightdeliveriesdetails.domain.FlightDeliveriesDetailsInteractor
import io.reactivex.disposables.CompositeDisposable

class FlightDeliveriesDetailsViewModel(
    private val parameters: FlightDeliveriesDetailsParameters,
    compositeDisposable: CompositeDisposable,
    interactor: FlightDeliveriesDetailsInteractor,
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