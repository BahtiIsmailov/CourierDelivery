package ru.wb.go.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.app.FlightStatus
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.AppVersionState
import ru.wb.go.ui.splash.domain.AppInteractor
import ru.wb.go.utils.LogUtils
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

    private val _appVersionState = MutableLiveData<AppVersionState>()
    val appVersionState: LiveData<AppVersionState>
        get() = _appVersionState

    init {
        fetchNetworkState()
        updateDrawer()
        observeUpdatedStatus()
        observeCountBoxes()
    }

    fun onSearchChange(query: String) {
        interactor.onSearchChange(query)
    }

    private fun observeUpdatedStatus() {
        addSubscription(interactor.observeUpdatedStatus()
            .map {
                when (it.flightStatus) {
                    FlightStatus.ASSIGNED, FlightStatus.DCLOADING, FlightStatus.DCUNLOADING, FlightStatus.UNLOADING ->
                        AppUIState.Loading(resourceProvider.getDeliveryId(it.flightId))
                    FlightStatus.INTRANSIT -> AppUIState.InTransit(
                        resourceProvider.getDeliveryId(
                            it.flightId
                        )
                    )
                    FlightStatus.CLOSED -> AppUIState.NotAssigned("Доставка")
                }
            }
            .subscribe({
                LogUtils { logDebugApp(it.toString()) }
                _flightsActionState.value = it

            }, {
                LogUtils { logDebugApp(it.toString()) }
            })
        )
    }

    private fun observeCountBoxes() {
        addSubscription(interactor.observeCountBoxes()
            .map {
                with(it) {
                    if (debtCount > 0) {
                        CounterBoxesActionStatus.AcceptedDebt(
                            resourceProvider.getCount(acceptedCount),
                            resourceProvider.getCount(returnCount),
                            resourceProvider.getCount(deliveryCount),
                            resourceProvider.getCount(debtCount)
                        )
                    } else {
                        CounterBoxesActionStatus.Accepted(
                            resourceProvider.getCount(acceptedCount),
                            resourceProvider.getCount(returnCount),
                            resourceProvider.getCount(deliveryCount),
                            resourceProvider.getCount(debtCount)
                        )
                    }
                }
            }
            .subscribe({ _counterBoxesActionStatus.value = it }) {})
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

    fun checkUpdateVersionApp() {
        // TODO: 14.11.2021 выключено до актуализации FTP сервера
//        _appVersionState.value = AppVersionState.UpToDateProgress
//        addSubscription(interactor.checkUpdateApp()
//            .subscribe({ checkUpdateVersionAppComplete(it) }, { checkUpdateVersionAppError() }))
    }

    private fun checkUpdateVersionAppComplete(appVersionState: AppVersionState) {
        _appVersionState.value = appVersionState
    }

    private fun checkUpdateVersionAppError() {
        _appVersionState.value = AppVersionState.UpdateError
    }

    fun updateVersionApp(destination: String) {
        // TODO: 14.11.2021 выключено до актуализации FTP сервера
//        _appVersionState.value = AppVersionState.UpdateProgress
//        addSubscription(interactor.getUpdateApp(destination)
//            .subscribe({ getUpdateAppComplete(it) }, { getUpdateAppError() }))
    }

    private fun getUpdateAppComplete(appVersionState: AppVersionState) {
        _appVersionState.value = appVersionState
    }

    private fun getUpdateAppError() {
        _appVersionState.value = AppVersionState.UpdateError
    }

}