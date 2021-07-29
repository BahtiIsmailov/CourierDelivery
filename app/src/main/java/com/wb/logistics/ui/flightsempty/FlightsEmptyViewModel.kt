package com.wb.logistics.ui.flightsempty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class FlightsEmptyViewModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<FlightsEmptyUINavUpdate>()

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    init {
        addSubscription(observeNetworkConnected().subscribe(
            { _toolbarNetworkState.value = it }, {}))
    }

    fun onRefresh() {
        stateUINav.value = FlightsEmptyUINavUpdate
    }

    private fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    object FlightsEmptyUINavUpdate

}