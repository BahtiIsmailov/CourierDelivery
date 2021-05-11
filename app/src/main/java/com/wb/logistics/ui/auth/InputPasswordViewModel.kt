package com.wb.logistics.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.auth.InputPasswordUIAction.*
import com.wb.logistics.ui.auth.InputPasswordUIState.*
import com.wb.logistics.ui.auth.domain.InputPasswordInteractor
import io.reactivex.disposables.CompositeDisposable

class InputPasswordViewModel(
    private val parameters: InputPasswordParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: InputPasswordInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUI = MutableLiveData<InputPasswordUIState>()
    val stateUI: LiveData<InputPasswordUIState>
        get() = _stateUI

    fun action(actionView: InputPasswordUIAction) {
        when (actionView) {
            is PasswordChanges -> fetchPasswordChanges(actionView.observable)
            RemindPassword -> {
                _stateUI.value = NavigateToTemporaryPassword(parameters.phone)
                _stateUI.value = Empty
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
        _stateUI.value = NavigateToApplication
    }

    private fun authError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> Error(throwable.message)
            else -> Error(throwable.toString())
        }
    }

}