package ru.wb.go.ui.flightsloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.flightsloader.domain.FlightDefinitionAction
import ru.wb.go.ui.flightsloader.domain.FlightsLoaderInteractor
import ru.wb.go.utils.LogUtils

class FlightLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: FlightsLoaderInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navHeader = MutableLiveData<UserInfoEntity>()
    val navHeader: LiveData<UserInfoEntity>
        get() = _navHeader

    private val _flightLoaderUIState = SingleLiveEvent<FlightLoaderUIState>()
    val flightLoaderUIState: LiveData<FlightLoaderUIState>
        get() = _flightLoaderUIState

    init {
        observeNetworkState()
        _flightLoaderUIState.value = FlightLoaderUIState.InitProgress
    }

    fun update() {
        toApp()
    }

    private fun toApp() {
        initSessionInfo()
        // TODO: 19.08.2021 выключено до реализации функционала по принятию документов курьера
//        addSubscription(
//            interactor.updateFlight().subscribe(
//                { navigateToOnComplete(it) },
//                { navigateToOnError(it) })
//        )
 //
    }

    private fun initSessionInfo() {
        addSubscription(
            interactor.sessionInfo().subscribe(
                { _navHeader.value = it },
                {
                    LogUtils { logDebugApp("initSessionInfoError " + it) }
                    _navHeader.value = UserInfoEntity("Ошибка", "Error") })
        )
    }

    private fun navigateToOnComplete(definitionAction: FlightDefinitionAction) {
        _flightLoaderUIState.value = when (definitionAction) {
            FlightDefinitionAction.FlightEmpty -> {
                FlightLoaderUIState.NotAssigned
            }
            is FlightDefinitionAction.NavigateToDirections -> {
                FlightLoaderUIState.InTransit(definitionAction.navDirections)
            }
        }
    }

    private fun navigateToOnError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> throwable.message ?: "Сервис временно недоступен\nПовторите действие чуть позже"
        }
        _flightLoaderUIState.value = FlightLoaderUIState.Error(message)
    }

    fun onUpdate() {
        _flightLoaderUIState.value = FlightLoaderUIState.Progress
        toApp()
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected()
            .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

}