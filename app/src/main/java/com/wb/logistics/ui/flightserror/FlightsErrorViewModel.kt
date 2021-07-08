package com.wb.logistics.ui.flightserror

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import io.reactivex.disposables.CompositeDisposable

class FlightsErrorViewModel(
    parameters: FlightsErrorParameters,
    compositeDisposable: CompositeDisposable,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<FlightsEmptyUINavUpdate>()
    val stateUI = MutableLiveData<FlightsMessageUpdate>()

    init {
        stateUI.value = FlightsMessageUpdate(parameters.message)
    }

    fun onRefresh() {
        stateUINav.value = FlightsEmptyUINavUpdate
    }

    object FlightsEmptyUINavUpdate
    data class FlightsMessageUpdate(val message: String)

}