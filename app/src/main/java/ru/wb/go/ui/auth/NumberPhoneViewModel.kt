package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.NumberPhoneUIState.*
import ru.wb.go.ui.auth.domain.NumberPhoneInteractor
import ru.wb.go.ui.auth.keyboard.KeyboardNumericView
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.formatter.PhoneUtils

class NumberPhoneViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: AuthResourceProvider,
    private val interactor: NumberPhoneInteractor,
) : NetworkViewModel(compositeDisposable, metric) {

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

    private fun observeNetworkState() {
        viewModelScope.launch {
            _toolbarNetworkState.postValue(interactor.observeNetworkConnected())
        }
    }

    fun onCheckPhone(number: String) {
        onTechEventLog("onCheckPhone", number)
        fetchPhoneNumber(number)
    }

//    fun onNumberObservableClicked(event:  KeyboardNumericView.ButtonAction ) {
//        val accumulator = interactor.userPhone()
//        val eventKeyboard = accumulateNumber(accumulator, event)
//        viewModelScope.launch {
//            try {
//                switchNext(eventKeyboard)
//                val it = numberToPhoneSpanFormat(eventKeyboard)
//                _stateUI.postValue(it)
//            }catch (e:Exception){
//                onTechErrorLog("onNumberObservableClicked", e)
//            }
//        }
//
//    }

//    fun onNumberObservableClicked(event: Observable<KeyboardNumericView.ButtonAction>) {
//        val initPhone = Observable.just(interactor.userPhone())
//        val eventKeyboard =
//            event.scan(String()) { accumulator, item -> accumulateNumber(accumulator, item) }
//        addSubscription(
//            initPhone.concatWith(eventKeyboard)
//                .doOnNext { switchNext(it) }
//                .map { numberToPhoneSpanFormat(it) }
//                .subscribe(
//                    { _stateUI.postValue(it) },
//                    { onTechErrorLog("onNumberObservableClicked", it) })
//        )
//    }

    fun onNumberObservableClicked(event: KeyboardNumericView.ButtonAction) {
        val initPhone = interactor.userPhone()
        viewModelScope.launch {
            try {
                val it = accumulateNumber(event.name, event)
                switchNext(it)
                numberToPhoneSpanFormat(it)
                _stateUI.postValue() // это не точно
            }catch (e:Exception){
                onTechErrorLog("onNumberObservableClicked", e)
            }
        }
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
        onTechEventLog("onCheckPhone", phone)
        _stateUI.postValue(NumberCheckProgress)
        viewModelScope.launch {
            try {
                interactor.couriersExistAndSavePhone(phone.filter { it.isDigit() })
                fetchPhoneNumberComplete(phone)
            }catch (e:Exception){
                fetchPhoneNumberError(e, phone)
            }
        }
    }

//    private fun fetchPhoneNumber(phone: String) {
//        _stateUI.postValue( NumberCheckProgress
//        val disposable = interactor.couriersExistAndSavePhone(phone.filter { it.isDigit() })
//            .subscribe(
//                { fetchPhoneNumberComplete(phone) },
//                { fetchPhoneNumberError(it, phone) }
//            )
//        addSubscription(disposable)
//    }

    private fun fetchPhoneNumberComplete(phone: String) {
        onTechEventLog("fetchPhoneNumberComplete", phone)
        _navigationEvent.postValue(NumberPhoneNavAction.NavigateToCheckPassword(phone, DEFAULT_TTL))
        _stateUI.postValue(NumberFormatComplete)
    }

    private fun fetchPhoneNumberError(throwable: Throwable, phone: String) {
        onTechErrorLog("fetchPhoneNumberError $phone", throwable)
        when (throwable) {
            is NoInternetException -> _stateUI.postValue(Error(
                resourceProvider.getGenericInternetTitleError(),
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            ))
            is BadRequestException -> {
                if (throwable.error.code == CODE_SENT) {
                    val ttl = throwable.error.data?.ttl ?: DEFAULT_TTL
                    _navigationEvent.postValue(
                        NumberPhoneNavAction.NavigateToCheckPassword(phone, ttl))
                    _stateUI.postValue(NumberFormatComplete)
                } else {
                    _stateUI.postValue(NumberNotFound(
                        resourceProvider.getGenericServiceTitleError(),
                        throwable.error.message,
                        resourceProvider.getGenericServiceButtonError()
                    ))
                }
            }
            else -> _stateUI.postValue( Error(
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            ))
        }
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val CODE_SENT = "CODE_SENT"
        const val NUMBER_LENGTH_MAX = 10
        const val NUMBER_DROP_COUNT_LAST = 1
        const val DEFAULT_TTL = 0
        const val SCREEN_TAG = "NumberPhone"
    }

}