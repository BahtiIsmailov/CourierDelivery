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
import ru.wb.go.ui.auth.domain.CheckSmsInteractor
import ru.wb.go.ui.auth.keyboard.KeyboardNumericView
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateHandler
import java.net.UnknownHostException

class CheckSmsViewModel(
    private val parameters: CheckSmsParameters,
    private val interactor: CheckSmsInteractor,
    private val resourceProvider: AuthResourceProvider,
) : TimerStateHandler, NetworkViewModel() {

    private val _stateTitleUI = MutableLiveData<InitTitle>()
    val stateTitleUI: LiveData<InitTitle>
        get() = _stateTitleUI

    private val _navigationEvent =
        SingleLiveEvent<CheckSmsNavigationState>()
    val navigationEvent: LiveData<CheckSmsNavigationState>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _checkSmsUIState = MutableLiveData<CheckSmsUIState>()
    val checkSmsUIState: LiveData<CheckSmsUIState>
        get() = _checkSmsUIState

    private val _repeatStateUI = MutableLiveData<CheckSmsUIRepeatState>()
    val repeatStateUI: LiveData<CheckSmsUIRepeatState>
        get() = _repeatStateUI

    private val _stateKeyboardBackspaceUI = SingleLiveEvent<CheckSmsBackspaceUIState>()
    val stateBackspaceUI: LiveData<CheckSmsBackspaceUIState>
        get() = _stateKeyboardBackspaceUI

    init {
        observeNetworkState()
        fetchTitle()
        subscribeTimer()
        restartTimer(if (parameters.ttl > 0) parameters.ttl else DURATION_TIME_INIT)
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

    private fun fetchTitle() {
        _stateTitleUI.value =
            InitTitle(resourceProvider.getTitleCheckSms(parameters.phone), parameters.phone)

    }

    private fun subscribeTimer() {
        interactor.timer
            .onEach {
                onHandleSignUpTimerState(it)
            }
            .catch {
                logException(it,"subscribeTimer")
                onHandleSignUpTimerError(it)
            }
            .launchIn(viewModelScope)

    }

    fun onNumberObservableClicked(event: Flow<KeyboardNumericView.ButtonAction>) {
        event.scan("") { accumulator, item ->
            accumulateCode(accumulator, item)
        }
            .onEach {
                switchNext(it)
            }
            .onEach {
                formatSmsComplete(it)
            }
            .catch {
                logException(it,"onNumberObservableClicked")
                formatSmsError(it)
            }
            .launchIn(viewModelScope)

    }

    private fun formatSmsError(throwable: Throwable) {
        onTechErrorLog("formatSmsError", throwable)
    }

    private fun formatSmsComplete(code: String) {
        onTechEventLog("formatSmsComplete", "code " + code.length)
        _checkSmsUIState.value = CheckSmsUIState.CodeFormat(code)
        if (code.length == NUMBER_LENGTH_MAX) fetchAuth(code)
    }

    private fun accumulateCode(accumulator: String, item: KeyboardNumericView.ButtonAction) =
        if (item == KeyboardNumericView.ButtonAction.BUTTON_DELETE) {
            accumulator.dropLast(NumberPhoneViewModel.NUMBER_DROP_COUNT_LAST)
        } else if (item == KeyboardNumericView.ButtonAction.BUTTON_DELETE_LONG) {
            accumulator.drop(accumulator.length)
        } else {
            if (accumulator.length > NUMBER_LENGTH_MAX - 1) accumulator.take(NUMBER_LENGTH_MAX)
            else accumulator.plus(item.ordinal)
        }

    private fun switchNext(code: String) {
        _stateKeyboardBackspaceUI.value =
            if (code.isEmpty()) CheckSmsBackspaceUIState.Inactive else CheckSmsBackspaceUIState.Active

    }

    private fun onHandleSignUpTimerState(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun onHandleSignUpTimerError(throwable: Throwable) {
        onTechErrorLog("onHandleSignUpError", throwable)
    }

    fun onRepeatPassword() {
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordProgress
        viewModelScope.launch {
            try {
                interactor.couriersExistAndSavePhone(formatPhone())
                fetchingPasswordComplete()
            } catch (e: Exception) {
                logException(e,"onRepeatPassword")
                fetchingPasswordError(e)
            }

        }
    }


    private fun fetchingPasswordComplete() {
        onTechEventLog("fetchingPasswordComplete")
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordComplete
    }

    private fun fetchingPasswordError(throwable: Throwable) {
        onTechErrorLog("fetchingPasswordError", throwable)
        when (throwable) {
            is NoInternetException -> _repeatStateUI.value =
                CheckSmsUIRepeatState.ErrorPassword(
                    resourceProvider.getGenericInternetTitleError(),
                    resourceProvider.getGenericInternetMessageError(),
                    resourceProvider.getGenericInternetButtonError()
                )
            is BadRequestException -> {
                with(throwable.error) {
                    restartTimer(
                        if (data == null) {
                            DURATION_TIME_INIT
                        } else {
                            if (data.ttl == 0) DURATION_TIME_INIT else data.ttl
                        }
                    )
                }
            }
            else -> _repeatStateUI.value =
                CheckSmsUIRepeatState.ErrorPassword(
                    resourceProvider.getGenericServiceTitleError(),
                    throwable.toString(),
                    resourceProvider.getGenericServiceButtonError()
                )

        }
    }

    private fun restartTimer(durationTime: Int) {
        viewModelScope.launch {
            interactor.startTimer(durationTime)
        }
    }

    private fun fetchAuth(password: String) {
        _checkSmsUIState.value = CheckSmsUIState.Progress
        val phone = formatPhone()
        viewModelScope.launch  {
            try {
                interactor.auth(phone, password)
                authComplete()
            } catch (e: Exception) {
                logException(e,"fetchAuth")
                authError(e)
            }
        }
    }

    private fun authComplete() {
        onTechEventLog("authComplete", "NavigateToAppLoader")
        _checkSmsUIState.value = CheckSmsUIState.Complete
        _navigationEvent.value = CheckSmsNavigationState.NavigateToAppLoader
    }

    private fun authError(throwable: Throwable) {
        onTechErrorLog("authError", throwable)
        _checkSmsUIState.value = when (throwable) {
            is NoInternetException,is UnknownHostException -> CheckSmsUIState.MessageError(
                resourceProvider.getGenericInternetTitleError(),
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException  -> {
                with(throwable.error) {
                    val restartTimer = if (data == null) {
                        DURATION_TIME_INIT
                    } else {
                        if (data.ttl == 0) DURATION_TIME_INIT else data.ttl
                    }
                    restartTimer(restartTimer)
                    CheckSmsUIState.MessageError(
                        "Неверный код",
                        "Проверте правильность ввода",
                        "Понятно"
                    )
                }
            }
            else -> CheckSmsUIState.MessageError(
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
    }

    private fun formatPhone() = parameters.phone.filter { it.isDigit() }

    override fun onTimerState(duration: Int, downTickSec: Int) {
        val time: String =
            resourceProvider.getSignUpTimeConfirmCode(getMin(downTickSec), getSec(downTickSec))
        _repeatStateUI.value =
            CheckSmsUIRepeatState.RepeatPasswordTimer(
                time,
                resourceProvider.getTitleInputTimerSpan()
            )

    }

    private fun getMin(duration: Int): Int {
        return duration / TIME_DIVIDER
    }

    private fun getSec(duration: Int): Int {
        return duration % TIME_DIVIDER
    }

    override fun onTimeIsOverState() {
        onTechEventLog("onTimeIsOverState")
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordComplete
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            interactor.stopTimer()
        }
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        private const val DURATION_TIME_INIT = 60
        const val TIME_DIVIDER = 60
        const val NUMBER_LENGTH_MAX = 4
        const val SCREEN_TAG = "CheckSms"
    }

    data class InitTitle(val title: String, val phone: String)

}