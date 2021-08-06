package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.InputPasswordUIAction.*
import ru.wb.perevozka.ui.auth.InputPasswordUIState.*
import ru.wb.perevozka.ui.auth.domain.InputPasswordInteractor
import io.reactivex.disposables.CompositeDisposable

class InputPasswordViewModel(
    private val parameters: InputPasswordParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: InputPasswordInteractor,
    private val resourceProvider: AuthResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<InputPasswordNavAction>()
    val navigationEvent: LiveData<InputPasswordNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _stateUI = MutableLiveData<InputPasswordUIState>()
    val stateUI: LiveData<InputPasswordUIState>
        get() = _stateUI

    init {
        observeNetworkState()
    }

    fun action(actionView: InputPasswordUIAction) {
        when (actionView) {
            is PasswordChanges -> fetchPasswordChanges(actionView.observable)
            RemindPassword -> {
                _navigationEvent.value = InputPasswordNavAction.NavigateToTemporaryPassword(parameters.phone)
            }
            is Auth -> fetchAuth(actionView.password)
        }
    }

    private fun fetchPasswordChanges(observable: InitialValueObservable<CharSequence>) {
        addSubscription(
            interactor.remindPasswordChanges(observable)
                .subscribe(
                    { _stateUI.value = if (it) NextEnable else NextDisable },
                    { _stateUI.value = NextDisable }
                )
        )
    }

    private fun fetchAuth(password: String) {
        _stateUI.value = AuthProcess
        val phone = formatPhone()
        addSubscription(interactor.authByPassword(phone, password)
            .subscribe(
                { authComplete() },
                { authError(it) }
            )
        )
    }

    private fun formatPhone() = parameters.phone.filter { it.isDigit() }

    private fun authComplete() {
        _navigationEvent.value = InputPasswordNavAction.NavigateToApplication
    }

    private fun authError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> PasswordNotFound(resourceProvider.getPasswordNotFound())
            else -> Error(resourceProvider.getGenericError())
        }
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {}))
    }

}