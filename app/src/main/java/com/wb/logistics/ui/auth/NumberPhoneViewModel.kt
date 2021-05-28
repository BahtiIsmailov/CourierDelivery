package com.wb.logistics.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.auth.NumberPhoneUIState.*
import com.wb.logistics.utils.formatter.PhoneUtils
import io.reactivex.disposables.CompositeDisposable

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    private val authRepository: AuthRemoteRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<NumberPhoneNavAction>()
    val navigationEvent: LiveData<NumberPhoneNavAction>
        get() = _navigationEvent

    private val _stateUI = MutableLiveData<NumberPhoneUIState<String>>()
    val stateUI: LiveData<NumberPhoneUIState<String>>
        get() = _stateUI

    fun action(actionView: NumberPhoneUIAction) {
        when (actionView) {
            is NumberPhoneUIAction.CheckPhone -> {
                fetchPhoneNumber(actionView.number)
                _stateUI.value = PhoneCheck
            }
            NumberPhoneUIAction.LongTitle ->
                _navigationEvent.value = NumberPhoneNavAction.NavigateToConfig
            is NumberPhoneUIAction.NumberChanges -> fetchPhoneNumberFormat(actionView)

        }
    }

    private fun fetchPhoneNumber(phone: String) {
        val disposable = authRepository.checkExistPhone(phone.filter { it.isDigit() })
            .compose(rxSchedulerFactory.applySingleSchedulers())
            .subscribe(
                { fetchPhoneNumberComplete(it, phone) },
                { fetchPhoneNumberError(it) }
            )
        addSubscription(disposable)
    }

    private fun fetchPhoneNumberFormat(actionView: NumberPhoneUIAction.NumberChanges) {
        addSubscription(
            PhoneUtils.phoneFormatter(actionView.observable, rxSchedulerFactory)
                .subscribe { number ->
                    _stateUI.value = if (number.length == PHONE_MAX_LENGTH)
                        NumberFormatComplete else NumberFormat(number)
                })
    }

    private fun fetchPhoneNumberComplete(checkPhoneRemote: CheckExistPhoneResponse, phone: String) {
        if (checkPhoneRemote.hasPassword)
            _navigationEvent.value = NumberPhoneNavAction.NavigateToInputPassword(phone)
        else NumberPhoneNavAction.NavigateToTemporaryPassword(phone)
    }

    private fun fetchPhoneNumberError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> NumberNotFound(throwable.message)
            else -> Error(throwable.toString())
        }
    }

    companion object {
        const val PHONE_MAX_LENGTH = 18
    }

}