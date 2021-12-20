package ru.wb.go.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.splash.domain.AppInteractor
import ru.wb.go.utils.managers.DeviceManager

class AppViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: AppInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager,

    ) : NetworkViewModel(compositeDisposable) {

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _flightsActionState = SingleLiveEvent<AppUIState>()
    val flightsActionState: LiveData<AppUIState>
        get() = _flightsActionState

    private val _counterBoxesActionStatus = MutableLiveData<CounterBoxesActionStatus>()
    val counterBoxesActionStatus: LiveData<CounterBoxesActionStatus>
        get() = _counterBoxesActionStatus

    init {
        fetchNetworkState()
        updateDrawer()
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun fetchNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _networkState.value = it }, {})
        )
    }

    fun onExitClick() {
        interactor.exitAuth()
    }

    fun onBillingClick() {
        //interactor.exitAuth()
    }

    private fun updateDrawer() {
        fetchVersionApp()
    }

}