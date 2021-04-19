package com.wb.logistics.ui.auth

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.auth.NumberPhoneUIState.*
import com.wb.logistics.utils.formatter.PhoneUtils
import io.reactivex.disposables.CompositeDisposable

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    private val authRepository: AuthRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<NumberPhoneUIState<String>>()

    fun action(actionView: NumberPhoneUIAction) {
        val state = when (actionView) {
            is NumberPhoneUIAction.CheckPhone -> {
                fetchPhoneNumber(actionView.number)
                PhoneCheck
            }
            NumberPhoneUIAction.LongTitle -> NavigateToConfig
            is NumberPhoneUIAction.NumberChanges -> {
                fetchPhoneNumberFormat(actionView)
//                Empty
                NumberFormat("+7 (925) 123-11-51")
            }
        }
        stateUI.value = state
    }

    private fun fetchPhoneNumber(phone: String) {
        val disposable = authRepository.checkExistPhone(phone.filter { it.isDigit() })
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
                    stateUI.value = if (number.length == PHONE_MAX_LENGTH)
                        NumberFormatComplete else NumberFormat(number)
                })
    }

    private fun fetchPhoneNumberComplete(checkPhoneRemote: CheckExistPhoneResponse, phone: String) {
        stateUI.value =
            if (checkPhoneRemote.hasPassword)
                NavigateToInputPassword(phone) else NavigateToTemporaryPassword(phone)
        stateUI.value = Empty
    }

    private fun fetchPhoneNumberError(throwable: Throwable) {
        stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> NumberNotFound(throwable.message)
            else -> Error(throwable.toString())
        }
    }

    companion object {
        const val PHONE_MAX_LENGTH = 18
    }

}