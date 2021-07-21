package com.wb.logistics.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.flightsloader.FlightActionStatus
import com.wb.logistics.ui.splash.domain.AppInteractor
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.utils.managers.DeviceManager
import io.reactivex.disposables.CompositeDisposable

class AppViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: AppInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager,

    ) : NetworkViewModel(compositeDisposable) {

    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean>
        get() = _networkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _flightsActionState = SingleLiveEvent<FlightActionStatus>()
    val flightsActionState: LiveData<FlightActionStatus>
        get() = _flightsActionState

    private val _counterBoxesActionStatus = MutableLiveData<CounterBoxesActionStatus>()
    val counterBoxesActionStatus: LiveData<CounterBoxesActionStatus>
        get() = _counterBoxesActionStatus

    init {
        fetchNetworkState()
        updateDrawer()
        observeUpdatedStatus()
        observeCountBoxes()
    }

    private fun observeUpdatedStatus() {
        addSubscription(interactor.observeUpdatedStatus()
            .map {
                when (it.flightStatus) {
                    FlightStatus.ASSIGNED, FlightStatus.DCLOADING, FlightStatus.DCUNLOADING, FlightStatus.UNLOADING ->
                        FlightActionStatus.Loading(resourceProvider.getDeliveryId(it.flightId))
                    FlightStatus.INTRANSIT -> FlightActionStatus.InTransit(resourceProvider.getDeliveryId(it.flightId))
                    FlightStatus.CLOSED -> FlightActionStatus.NotAssigned("Доставка")
                }
            }
            .subscribe({
                LogUtils { logDebugApp(it.toString()) }
                _flightsActionState.value = it

            }, {
                LogUtils { logDebugApp(it.toString()) }

            }))
    }

    private fun observeCountBoxes() {
        addSubscription(interactor.observeCountBoxes()
            .map {
                with(it) {
                    val debt = attachedCount - unloadedCount
                    if (debt == 0) {
                        CounterBoxesActionStatus.Accepted(
                            resourceProvider.getCount(unloadedCount),
                            resourceProvider.getCount(debt))
                    } else {
                        CounterBoxesActionStatus.AcceptedDebt(
                            resourceProvider.getCount(unloadedCount),
                            resourceProvider.getCount(debt))
                    }
                }
            }
            .subscribe({ _counterBoxesActionStatus.value = it }) {})
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun fetchNetworkState() {
        addSubscription(interactor.isNetworkConnected().subscribe({ _networkState.value = it }, {}))
    }

    fun onExitClick() {
        interactor.exitAuth()
    }

    private fun updateDrawer() {
        fetchVersionApp()
    }

}