package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.NumberPhoneUIState.*
import ru.wb.go.ui.auth.domain.NumberPhoneInteractor
import ru.wb.go.ui.auth.keyboard.KeyboardNumericView
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.formatter.PhoneUtils

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: AuthResourceProvider,
    private val interactor: NumberPhoneInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<NumberPhoneNavAction>()
    val navigationEvent: LiveData<NumberPhoneNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _stateUI = SingleLiveEvent<NumberPhoneUIState>()
    val stateUI: LiveData<NumberPhoneUIState>
        get() = _stateUI

    private val _stateKeyboardBackspaceUI = SingleLiveEvent<NumberPhoneBackspaceUIState>()
    val stateBackspaceUI: LiveData<NumberPhoneBackspaceUIState>
        get() = _stateKeyboardBackspaceUI

    init {
        observeNetworkState()
    }

    fun onCheckPhone(number: String) {
        fetchPhoneNumber(number)
    }

    fun onLongClick() {
        navigateToConfig()
    }

    fun onNumberObservableClicked(event: Observable<KeyboardNumericView.ButtonAction>) {
        val initPhone = Observable.just(interactor.userPhone())
        val eventKeyboard =
            event.scan(String(), { accumulator, item -> accumulateNumber(accumulator, item) })
        addSubscription(
            initPhone.concatWith(eventKeyboard)
                .doOnNext { switchNext(it) }
                .map { numberToPhoneSpanFormat(it) }
                .subscribe(
                    { _stateUI.value = it },
                    { LogUtils { logDebugApp("onNumberObservableClicked err " + it.toString()) } })
        )
    }

    private fun numberToPhoneSpanFormat(it: String) = PhoneSpanFormat(
        PhoneUtils.phoneFormatter(it),
        PhoneUtils.phoneFormatterSpanLength(it)
    )

    private fun switchNext(it: String) {
        _stateKeyboardBackspaceUI.value =
            if (it.isEmpty()) NumberPhoneBackspaceUIState.Inactive else NumberPhoneBackspaceUIState.Active
        _stateUI.value =
            if (it.length < NUMBER_LENGTH_MAX) NumberNotFilled else NumberFormatComplete
    }


    private fun accumulateNumber(accumulator: String, item: KeyboardNumericView.ButtonAction) =
        if (item == KeyboardNumericView.ButtonAction.BUTTON_DELETE) {
            accumulator.dropLast(NUMBER_DROP_COUNT_LAST)
        } else if (item == KeyboardNumericView.ButtonAction.BUTTON_DELETE_LONG) {
            accumulator.drop(accumulator.length)
        } else {
            if (accumulator.length > NUMBER_LENGTH_MAX - 1) accumulator.take(NUMBER_LENGTH_MAX)
            else accumulator.plus(item.ordinal)
        }

    private fun fetchPhoneNumber(phone: String) {
        _stateUI.value = NumberCheckProgress
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
        _navigationEvent.value = NumberPhoneNavAction.NavigateToCheckPassword(phone, DEFAULT_TTL)
        _stateUI.value = NumberFormatComplete
    }

    private fun fetchPhoneNumberError(throwable: Throwable, phone: String) {
        when (throwable) {
            is NoInternetException -> _stateUI.value = Error(
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> {
                if (throwable.error.code == CODE_SENT) {
                    val ttl = throwable.error.data?.ttl ?: DEFAULT_TTL
                    _navigationEvent.value =
                        NumberPhoneNavAction.NavigateToCheckPassword(phone, ttl)
                    _stateUI.value = NumberFormatComplete
                } else {
                    _stateUI.value = NumberNotFound(
                        throwable.error.message,
                        resourceProvider.getGenericServiceMessageError(),
                        resourceProvider.getGenericServiceButtonError()
                    )
                }
            }
            else -> _stateUI.value = Error(
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    companion object {
        const val CODE_SENT = "CODE_SENT"
        const val NUMBER_LENGTH_MAX = 10
        const val NUMBER_DROP_COUNT_LAST = 1
        const val DEFAULT_TTL = 0
    }

}