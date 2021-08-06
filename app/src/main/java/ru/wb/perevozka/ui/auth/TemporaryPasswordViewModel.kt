package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.TemporaryPasswordUIAction.CheckPassword
import ru.wb.perevozka.ui.auth.TemporaryPasswordUIAction.PasswordChanges
import ru.wb.perevozka.ui.auth.TemporaryPasswordUIState.*
import ru.wb.perevozka.ui.auth.domain.TemporaryPasswordInteractor
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateHandler
import io.reactivex.disposables.CompositeDisposable

class TemporaryPasswordViewModel(
    private val parameters: TemporaryPasswordParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: TemporaryPasswordInteractor,
    private val resourceProvider: AuthResourceProvider,
) : TimerStateHandler, NetworkViewModel(compositeDisposable) {

    private val _stateTitleUI = MutableLiveData<InitTitle>()
    val stateTitleUI: LiveData<InitTitle>
        get() = _stateTitleUI

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigationEvent =
        SingleLiveEvent<TemporaryPasswordNavAction>()
    val navigationEvent: LiveData<TemporaryPasswordNavAction>
        get() = _navigationEvent

    private val _repeatStateUI = MutableLiveData<TemporaryPasswordUIRepeatState>()
    val repeatStateUI: LiveData<TemporaryPasswordUIRepeatState>
        get() = _repeatStateUI

    private val _stateUI = MutableLiveData<TemporaryPasswordUIState>()
    val stateUI: LiveData<TemporaryPasswordUIState>
        get() = _stateUI

    init {
        observeNetworkState()
        fetchTitle()
        fetchInitTmpPassword()
        subscribeTimer()
    }

    private fun fetchTitle() {
        _stateTitleUI.postValue(
            InitTitle(
                resourceProvider.getTitleTemporaryPassword(parameters.phone),
                parameters.phone
            )
        )
    }

    private fun subscribeTimer() {
        addSubscription(interactor.timer
            .subscribe({ onHandleSignUpState(it) })
            { onHandleSignUpError() })
    }

    private fun onHandleSignUpState(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun onHandleSignUpError() {}

    private fun fetchInitTmpPassword() {
        addSubscription(
            interactor.sendTmpPassword(formatPhone()).subscribe(
                { fetchingTmpPasswordComplete() },
                { fetchingTmpPasswordError(it) }
            )
        )
    }

    private fun fetchTmpPassword() {
        _repeatStateUI.value = TemporaryPasswordUIRepeatState.RepeatPasswordProgress
        fetchInitTmpPassword()
    }

    private fun fetchingTmpPasswordComplete() {
        _repeatStateUI.value = TemporaryPasswordUIRepeatState.RepeatPassword
    }

    override fun onCleared() {
        super.onCleared()
        interactor.stopTimer()
    }

    private fun fetchingTmpPasswordError(throwable: Throwable) {
        when (throwable) {
            is NoInternetException -> _repeatStateUI.value =
                TemporaryPasswordUIRepeatState.ErrorPassword(throwable.message)
            is BadRequestException -> {
                with(throwable.error) {
                    if (data != null) {
                        restartTimer(if (data.resendTime == 0) DURATION_TIME_INIT else data.resendTime)
                    }
                }
            }
            else -> _repeatStateUI.value =
                TemporaryPasswordUIRepeatState.ErrorPassword(resourceProvider.getGenericError())
        }
    }

    private fun restartTimer(durationTime: Int) {
        interactor.startTimer(durationTime)
    }

    fun action(actionView: TemporaryPasswordUIAction) {
        when (actionView) {
            is PasswordChanges -> fetchPasswordChanges(actionView.observable)
            is CheckPassword -> fetchTmpPasswordCheck(actionView.password)
        }
    }

    fun onRepeatTmpPassword() {
        fetchTmpPassword()
    }

    private fun fetchPasswordChanges(observable: InitialValueObservable<CharSequence>) {
        addSubscription(
            interactor.passwordChanges(observable)
                .subscribe({
                    _stateUI.value = if (it) NextEnable else NextDisable
                }, {
                    _stateUI.value = NextDisable
                })
        )
    }

    private fun fetchTmpPasswordCheck(tmpPassword: String) {
        _stateUI.value = Progress
        addSubscription(interactor.checkPassword(formatPhone(), tmpPassword)
            .subscribe(
                { tmpPasswordCheckComplete(tmpPassword) },
                { tmpPasswordCheckError(it) }
            )
        )
    }

    private fun formatPhone() = parameters.phone.filter { it.isDigit() }

    private fun tmpPasswordCheckComplete(tmpPassword: String) {
        _navigationEvent.value =
            TemporaryPasswordNavAction.NavigateToCreatePassword(parameters.phone, tmpPassword)
    }

    private fun tmpPasswordCheckError(throwable: Throwable) {
        when (throwable) {
            is NoInternetException -> _stateUI.value = Error(throwable.message)
            is BadRequestException -> _stateUI.value =
                PasswordNotFound(resourceProvider.getTemporaryPasswordNotFound())
            else -> _stateUI.value = Error(resourceProvider.getGenericError())
        }
    }

    override fun onTimerState(duration: Int) {
        val time: String =
            resourceProvider.getSignUpTimeConfirmCode(getMin(duration), getSec(duration))
        _repeatStateUI.value = TemporaryPasswordUIRepeatState.RepeatPasswordTimer(time,
            resourceProvider.getTitleInputTimerSpan())
    }

    private fun getMin(duration: Int): Int {
        return duration / TIME_DIVIDER
    }

    private fun getSec(duration: Int): Int {
        return duration % TIME_DIVIDER
    }

    override fun onTimeIsOverState() {
        _repeatStateUI.value = TemporaryPasswordUIRepeatState.RepeatPassword
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {}))
    }

    companion object {
        private const val DURATION_TIME_INIT = 30
        const val TIME_DIVIDER = 60
    }

}

data class InitTitle(val title: String, val phone: String)