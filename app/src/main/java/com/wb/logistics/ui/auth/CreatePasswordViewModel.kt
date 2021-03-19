package com.wb.logistics.ui.auth

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.auth.CreatePasswordUIAction.Auth
import com.wb.logistics.ui.auth.CreatePasswordUIAction.PasswordChanges
import com.wb.logistics.ui.auth.CreatePasswordUIState.*
import com.wb.logistics.ui.auth.domain.CreatePasswordInteractor
import io.reactivex.disposables.CompositeDisposable

class CreatePasswordViewModel(
    private val parameters: CreatePasswordParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CreatePasswordInteractor
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<CreatePasswordUIState<String>>()

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
                    { stateUI.value = if (it) SaveAndNextEnable else SaveAndNextDisable },
                    { stateUI.value = SaveAndNextDisable }
                )
        )
    }

    private fun fetchAuth(password: String, tmpPassword: String) {
        stateUI.value = AuthProcess
        addSubscription(interactor.saveAndAuthByPassword(formatPhone(), password, tmpPassword)
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
        stateUI.value = NavigateToTemporaryPassword("")
    }

}