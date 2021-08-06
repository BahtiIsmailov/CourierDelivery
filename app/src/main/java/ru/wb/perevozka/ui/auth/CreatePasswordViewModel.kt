package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.CreatePasswordUIAction.Auth
import ru.wb.perevozka.ui.auth.CreatePasswordUIAction.PasswordChanges
import ru.wb.perevozka.ui.auth.CreatePasswordUIState.*
import ru.wb.perevozka.ui.auth.domain.CreatePasswordInteractor
import io.reactivex.disposables.CompositeDisposable

class CreatePasswordViewModel(
    private val parameters: CreatePasswordParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CreatePasswordInteractor,
    private val resourceProvider: AuthResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<CreatePasswordNavAction>()
    val navigationEvent: LiveData<CreatePasswordNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _stateUI = MutableLiveData<CreatePasswordUIState>()
    val stateUI: LiveData<CreatePasswordUIState>
        get() = _stateUI

    init {
        observeNetworkState()
    }

    fun action(actionView: CreatePasswordUIAction) {
        when (actionView) {
            is PasswordChanges -> fetchPasswordChanges(actionView.observable)
            is Auth -> fetchAuth(actionView.password, parameters.tmpPassword)
        }
    }

    private fun fetchPasswordChanges(observable: InitialValueObservable<CharSequence>) {
        addSubscription(
            interactor.remindPasswordChanges(observable)
                .subscribe(
                    { _stateUI.value = if (it) SaveAndNextEnable else SaveAndNextDisable },
                    { _stateUI.value = SaveAndNextDisable }
                )
        )
    }

    private fun fetchAuth(password: String, tmpPassword: String) {
        _stateUI.value = AuthProcess
        addSubscription(interactor.saveAndAuthByPassword(formatPhone(), password, tmpPassword)
            .subscribe(
                { authComplete() },
                { authError(it) }
            )
        )
    }

    private fun formatPhone() = parameters.phone.filter { it.isDigit() }

    private fun authComplete() {
        _stateUI.value = AuthComplete
        _navigationEvent.value = CreatePasswordNavAction.NavigateToApplication
    }

    private fun authError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> Error(throwable.error.message)
            else -> Error(resourceProvider.getGenericError())
        }
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {}))
    }

}