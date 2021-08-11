package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.network.token.UserManager
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.NumberPhoneUIState.*
import ru.wb.perevozka.ui.auth.domain.NumberPhoneInteractor
import ru.wb.perevozka.utils.formatter.PhoneUtils

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: AuthResourceProvider,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val interactor: NumberPhoneInteractor,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<NumberPhoneNavAction>()
    val navigationEvent: LiveData<NumberPhoneNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _stateUI = SingleLiveEvent<NumberPhoneUIState>()
    val stateUI: LiveData<NumberPhoneUIState>
        get() = _stateUI

    init {
        observeNetworkState()
        checkUserState()
    }

    private fun checkUserState() {
        if (tokenManager.isContains()) {
            val phone = tokenManager.userPhone()
            when {
                tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> toUserForm(phone)
                tokenManager.resources()
                    .contains(NEED_APPROVE_COURIER_DOCUMENTS) -> toCouriersCompleteRegistration(
                    phone
                )
                tokenManager.resources().isEmpty() -> toApp()
            }
        }
    }

    private fun toUserForm(phone: String) {
        _navigationEvent.value = NumberPhoneNavAction.NavigateToUserForm(phone)
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        _navigationEvent.value = NumberPhoneNavAction.NavigateToCouriersCompleteRegistration(phone)
    }

    private fun toApp() {
        _navigationEvent.value = NumberPhoneNavAction.NavigateToApp
    }


    fun initFormatter() {
        addSubscription(
            PhoneUtils.phoneFormatter(Observable.just(interactor.userPhone()), rxSchedulerFactory)
                .subscribe { number -> _stateUI.value = NumberFormatInit(number) })
    }

    fun action(actionView: NumberPhoneUIAction) {
        when (actionView) {
            is NumberPhoneUIAction.NumberChanges -> fetchPhoneNumberFormat(actionView)
            is NumberPhoneUIAction.CheckPhone -> fetchPhoneNumber(actionView.number)
            NumberPhoneUIAction.LongTitle -> navigateToConfig()
            NumberPhoneUIAction.NumberClear -> userManager.clear()
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
        val disposable = interactor.couriersExistAndSavePhone(phone.filter { it.isDigit() })
            .subscribe(
                { fetchPhoneNumberComplete(phone) },
                { fetchPhoneNumberError(it, phone) }
            )
        addSubscription(disposable)
    }

    private fun navigateToConfig() {
        _navigationEvent.value = NumberPhoneNavAction.NavigateToConfig
    }

    private fun fetchPhoneNumberComplete(phone: String) {
        _navigationEvent.value = NumberPhoneNavAction.NavigateToCheckPassword(phone, 0)
    }

    private fun fetchPhoneNumberError(throwable: Throwable, phone: String) {
        when (throwable) {
            is NoInternetException -> _stateUI.value = Error(throwable.message)
            is BadRequestException -> {
                if (throwable.error.code == "CODE_SENT") {
                    val ttl = throwable.error.data?.ttl ?: 0
                    _navigationEvent.value = NumberPhoneNavAction.NavigateToCheckPassword(phone, ttl)
                } else {
                    _stateUI.value = NumberNotFound(throwable.error.message)
                }
            }
            else -> _stateUI.value = Error(resourceProvider.getGenericError())
        }
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    companion object {
        const val PHONE_MAX_LENGTH = 18
    }

}