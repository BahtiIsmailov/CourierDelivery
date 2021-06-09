package com.wb.logistics.ui.auth

import androidx.lifecycle.LiveData
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.auth.NumberPhoneUIState.*
import com.wb.logistics.utils.formatter.PhoneUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    private val authRepository: AuthRemoteRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val resourceProvider: AuthResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<NumberPhoneNavAction>()
    val navigationEvent: LiveData<NumberPhoneNavAction>
        get() = _navigationEvent

    private val _stateUI = SingleLiveEvent<NumberPhoneUIState>()
    val stateUI: LiveData<NumberPhoneUIState>
        get() = _stateUI

    fun initFormatter() {
        addSubscription(
            PhoneUtils.phoneFormatter(Observable.just(authRepository.userPhone()), rxSchedulerFactory)
                .subscribe { number -> _stateUI.value = NumberFormatInit(number) })
    }

    fun action(actionView: NumberPhoneUIAction) {
        when (actionView) {
            is NumberPhoneUIAction.NumberChanges -> fetchPhoneNumberFormat(actionView)
            is NumberPhoneUIAction.CheckPhone -> fetchPhoneNumber(actionView.number)
            NumberPhoneUIAction.LongTitle -> navigateToConfig()
        }
    }

    private fun fetchPhoneNumberFormat(actionView: NumberPhoneUIAction.NumberChanges) {
        addSubscription(
            PhoneUtils.phoneFormatter(actionView.observable, rxSchedulerFactory)
                .subscribe { number ->
                    _stateUI.value = if (number.length == PHONE_MAX_LENGTH)
                        NumberFormatComplete else NumberFormat(number)
                })
    }

    private fun fetchPhoneNumber(phone: String) {
        _stateUI.value = PhoneCheck
        val disposable = authRepository.checkExistAndSavePhone(phone.filter { it.isDigit() })
            .compose(rxSchedulerFactory.applySingleSchedulers())
            .subscribe(
                { fetchPhoneNumberComplete(it, phone) },
                { fetchPhoneNumberError(it) }
            )
        addSubscription(disposable)
    }

    private fun navigateToConfig() {
        _navigationEvent.value = NumberPhoneNavAction.NavigateToConfig
    }

    private fun fetchPhoneNumberComplete(checkPhoneRemote: CheckExistPhoneResponse, phone: String) {
        _navigationEvent.value =
            if (checkPhoneRemote.hasPassword) NumberPhoneNavAction.NavigateToInputPassword(phone)
            else NumberPhoneNavAction.NavigateToTemporaryPassword(phone)
    }

    private fun fetchPhoneNumberError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> NumberNotFound(resourceProvider.getNumberNotFound())
            else -> Error(resourceProvider.getGenericError())
        }
    }

    companion object {
        const val PHONE_MAX_LENGTH = 18
    }

}