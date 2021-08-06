package ru.wb.perevozka.ui.flightsloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.perevozka.network.api.auth.entity.UserInfoEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.flightsloader.domain.FlightDefinitionAction
import ru.wb.perevozka.ui.flightsloader.domain.FlightsLoaderInteractor
import io.reactivex.disposables.CompositeDisposable

class FlightLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: FlightsLoaderInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

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
        addSubscription(interactor.sessionInfo().subscribe({ _navHeader.value = it }, {}))
        addSubscription(
            interactor.updateFlight().subscribe(
                { navigateToOnComplete(it) },
                { navigateToOnError(it) })
        )
    }

    private fun navigateToOnComplete(definitionAction: FlightDefinitionAction) {
        _flightLoaderUIState.value = when (definitionAction) {
            FlightDefinitionAction.FlightEmpty -> {
                FlightLoaderUIState.NotAssigned
            }
            is FlightDefinitionAction.NavigateComplete -> {
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
            .subscribe({ _toolbarNetworkState.value = it }, {}))
    }

}