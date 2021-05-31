package com.wb.logistics.ui.flightsempty

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import io.reactivex.disposables.CompositeDisposable

class FlightsEmptyViewModel(
    compositeDisposable: CompositeDisposable,
    resourceProvider: FlightsEmptyResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<FlightsEmptyUINavUpdate>()
    val stateUI = MutableLiveData<FlightsEmptyUIState>()

    init {
        stateUI.value = FlightsEmptyUIState(resourceProvider.getZeroFlight())
    }

    fun onRefresh() {
        stateUINav.value = FlightsEmptyUINavUpdate
    }

    object FlightsEmptyUINavUpdate
    data class FlightsEmptyUIState(val countFlight: String)

}