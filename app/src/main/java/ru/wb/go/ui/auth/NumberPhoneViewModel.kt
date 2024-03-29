package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.NumberPhoneUIState.*
import ru.wb.go.ui.auth.domain.NumberPhoneInteractor
import ru.wb.go.ui.auth.keyboard.KeyboardNumericView
import ru.wb.go.utils.formatter.PhoneUtils

class NumberPhoneViewModel(
    private val resourceProvider: AuthResourceProvider,
    private val interactor: NumberPhoneInteractor,
) : NetworkViewModel() {

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
        interactor.observeNetworkConnected()
            .onEach {
                _toolbarNetworkState.value = it
            }
            .catch {
                logException(it,"observeNetworkState")
            }
            .launchIn(viewModelScope)
    }
//        if (CheckInternet.checkConnection(App.getContext()!!)){
//            _toolbarNetworkState.value = NetworkState.Complete
//        }else{
//            _toolbarNetworkState.value = NetworkState.Failed
//        }



    fun onCheckPhone(number: String) {
        //onTechEventLog("onCheckPhone", number)
        fetchPhoneNumber(number)
    }


    fun onNumberObservableClicked(event: Flow<KeyboardNumericView.ButtonAction>) {
        interactor.userPhone()
        val eventKeyboard = event.scan("") {// previous value concatenate to the next
                accumulator, item ->
            accumulateNumber(accumulator, item)
        }
        eventKeyboard
            .onEach {
                switchNext(it)
            }
            .map {
                numberToPhoneSpanFormat(it)
            }
            .onEach {
                _stateUI.value = it
            }
            .catch {
                logException(it,"onNumberObservableClicked")
                //onTechEventLog("onNumberObservableClicked", it)
            }
            .launchIn(viewModelScope)

    }

    private fun numberToPhoneSpanFormat(it: String) = PhoneSpanFormat(
        PhoneUtils.phoneFormatter(it),
        PhoneUtils.phoneFormatterSpanLength(it)
    )

    private fun switchNext(it: String) {
        if (it.isEmpty()) {
            _stateKeyboardBackspaceUI.value =
                NumberPhoneBackspaceUIState.Inactive

        } else {
            _stateKeyboardBackspaceUI.value = NumberPhoneBackspaceUIState.Active
        }
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
        //onTechEventLog("onCheckPhone", phone)
        _stateUI.value = NumberCheckProgress
        viewModelScope.launch {
            try {
                interactor.couriersExistAndSavePhone(phone.filter { it.isDigit() })
                fetchPhoneNumberComplete(phone)
            } catch (e: Exception) {
                logException(e,"fetchPhoneNumber")
                fetchPhoneNumberError(e, phone)
            }
        }
    }


    private fun fetchPhoneNumberComplete(phone: String) {
        //onTechEventLog("fetchPhoneNumberComplete", phone)
        _navigationEvent.value = NumberPhoneNavAction.NavigateToCheckPassword(phone, DEFAULT_TTL)
        _stateUI.value = NumberFormatComplete
    }

    private fun fetchPhoneNumberError(throwable: Throwable, phone: String) {
        //onTechEventLog("fetchPhoneNumberError $phone", throwable)
        when (throwable) {
            is NoInternetException -> _stateUI.value =
                Error(
                    resourceProvider.getGenericInternetTitleError(),
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
                    _stateUI.value =
                        NumberNotFound(
                            resourceProvider.getGenericServiceTitleError(),
                            throwable.error.message,
                            resourceProvider.getGenericServiceButtonError()
                        )

                }
            }
            else -> _stateUI.value =
                Error(
                    resourceProvider.getGenericServiceTitleError(),
                    throwable.toString(),
                    resourceProvider.getGenericServiceButtonError()
                )
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