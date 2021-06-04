package com.wb.logistics.ui.flightsempty

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import io.reactivex.disposables.CompositeDisposable

class FlightsEmptyViewModel(
    compositeDisposable: CompositeDisposable,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<FlightsEmptyUINavUpdate>()

    fun onRefresh() {
        stateUINav.value = FlightsEmptyUINavUpdate
    }

    object FlightsEmptyUINavUpdate

}