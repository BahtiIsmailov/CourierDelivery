package com.wb.logistics.ui.auth

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

    val stateUI = MutableLiveData<InputPasswordUIState<String>>()

    fun action(actionView: InputPasswordUIAction) {
        when (actionView) {
            is PasswordChanges -> fetchPasswordChanges(actionView.observable)
            RemindPassword -> {
                stateUI.value = NavigateToTemporaryPassword(parameters.phone)
                stateUI.value = Empty
            }
            is Auth -> fetchAuth(actionView.password)
        }
    }

    private fun fetchPasswordChanges(observable: InitialValueObservable<CharSequence>) {
        addSubscription(
            interactor.remindPasswordChanges(observable)
                .subscribe(
                    { stateUI.value = if (it) NextEnable else NextDisable },
                    { stateUI.value = NextDisable }
                )
        )
    }

    private fun fetchAuth(password: String) {
        stateUI.value = AuthProcess
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
        stateUI.value = AuthComplete
        stateUI.value = NavigateToApplication
    }

    private fun authError(throwable: Throwable) {
        stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> Error(throwable.message)
            else -> Error(throwable.toString())
        }
    }

}