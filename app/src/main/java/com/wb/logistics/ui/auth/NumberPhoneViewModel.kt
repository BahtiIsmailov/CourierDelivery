package com.wb.logistics.ui.auth

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.api.AuthRepository
import com.wb.logistics.network.api.remote.CheckPhoneRemote
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.ForbiddenException
import com.wb.logistics.network.exceptions.LockedException
import com.wb.logistics.network.exceptions.UnauthorizedException
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.utils.formatter.PhoneUtils
import io.reactivex.disposables.CompositeDisposable

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    private val authRepository: AuthRepository,
    private val rxSchedulerFactory: RxSchedulerFactory
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<NumberPhoneUIState<String>>()

    fun action(actionView: NumberPhoneUIAction) {
        val state = when (actionView) {
            is NumberPhoneUIAction.CheckPhone -> {
                fetchPhoneNumber(actionView.number)
                NumberPhoneUIState.Loading
            }
            NumberPhoneUIAction.LongTitle -> NumberPhoneUIState.NavigateToConfig
            is NumberPhoneUIAction.NumberChanged -> {
                fetchPhoneNumberFormat(actionView)
                NumberPhoneUIState.Empty
            }
        }
        stateUI.value = state
    }

    private fun fetchPhoneNumber(phone: String) {
        val disposable = authRepository.checkExistPhone(phone.filter { it.isDigit() })
            .subscribe(
                { fetchPhoneNumberComplete(it) },
                { fetchPhoneNumberError(it) }
            )
        addSubscription(disposable)
    }

    private fun fetchPhoneNumberFormat(actionView: NumberPhoneUIAction.NumberChanged) {
        addSubscription(
            PhoneUtils.phoneFormatter(actionView.observable, rxSchedulerFactory)
                .subscribe { number ->
                    stateUI.value = if (number.length == PHONE_MAX_LENGTH)
                        NumberPhoneUIState.NumberFormatComplete else NumberPhoneUIState.NumberFormat(
                        number
                    )
                })
    }

    private fun fetchPhoneNumberComplete(checkPhoneRemote: CheckPhoneRemote) {
        stateUI.value = if (checkPhoneRemote.hasPassword)
            NumberPhoneUIState.NavigateToInput else NumberPhoneUIState.NavigateToTemporaryPassword
    }

    private fun fetchPhoneNumberError(throwable: Throwable) {
        stateUI.value = when (throwable) {
            is BadRequestException -> NumberPhoneUIState.PhoneNumberNotFound(throwable.message)
            is UnauthorizedException -> NumberPhoneUIState.PhoneNumberNotFound(throwable.message)
            is ForbiddenException -> NumberPhoneUIState.PhoneNumberNotFound(throwable.message)
            is LockedException -> NumberPhoneUIState.SMSAuthenticationLocked(throwable.message)
            else -> NumberPhoneUIState.Error(throwable.toString())
        }
    }

    companion object {
        const val PHONE_MAX_LENGTH = 18
    }

}