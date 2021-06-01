package com.wb.logistics.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.auth.TemporaryPasswordUIAction.CheckPassword
import com.wb.logistics.ui.auth.TemporaryPasswordUIAction.PasswordChanges
import com.wb.logistics.ui.auth.TemporaryPasswordUIState.*
import com.wb.logistics.ui.auth.domain.TemporaryPasswordInteractor
import com.wb.logistics.ui.auth.signup.TimerState
import com.wb.logistics.ui.auth.signup.TimerStateHandler
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable

class TemporaryPasswordViewModel(
    private val parameters: TemporaryPasswordParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: TemporaryPasswordInteractor,
    private val resourceProvider: AuthResourceProvider,
) : TimerStateHandler, NetworkViewModel(compositeDisposable) {

    private val _navigationEvent =
        SingleLiveEvent<TemporaryPasswordNavAction>()
    val navigationEvent: LiveData<TemporaryPasswordNavAction>
        get() = _navigationEvent

    private val _stateUI = MutableLiveData<TemporaryPasswordUIState>()
    val stateUI: LiveData<TemporaryPasswordUIState>
        get() = _stateUI

    init {
        fetchTitle()
        fetchTmpPassword()
    }

    private fun fetchTitle() {
        _stateUI.postValue(
            InitTitle(
                resourceProvider.getTitleTemporaryPassword(parameters.phone),
                parameters.phone
            )
        )
    }

    private fun subscribeTimer() {
        addSubscription(interactor.timer
            .subscribe({ onHandleSignUpState(it) })
            { onHandleSignUpError(it) })
    }

    private fun onHandleSignUpState(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun onHandleSignUpError(throwable: Throwable) {}

    private fun fetchTmpPassword() {
        _stateUI.value = FetchingTmpPassword
        addSubscription(
            interactor.sendTmpPassword(formatPhone()).subscribe(
                { fetchingTmpPasswordComplete(it) },
                { error(it) }
            )
        )
    }

    private fun fetchingTmpPasswordComplete(it: RemainingAttemptsResponse) {
        startTimer()
        subscribeTimer()
        _stateUI.value =
            RemainingAttempts(resourceProvider.getNumberAttempt(it.remainingAttempts))
    }

    private fun startTimer() {
        interactor.startTimer()
    }

    fun action(actionView: TemporaryPasswordUIAction) {
        when (actionView) {
            is PasswordChanges -> fetchPasswordChanges(actionView.observable)
            TemporaryPasswordUIAction.RepeatTmpPassword -> fetchTmpPassword()
            is CheckPassword -> fetchTmpPasswordCheck(actionView.password)
        }
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
        error(throwable)
    }

    private fun error(throwable: Throwable) {
        LogUtils { logDebugApp(throwable.toString()) }
        _stateUI.value = when (throwable) {
            is NoInternetException -> Error(throwable.message)
            is BadRequestException -> Update(throwable.message)
            else -> Error(throwable.toString())
        }
    }

    override fun onTimerState(duration: Int) {
        val time: String =
            resourceProvider.getSignUpTimeConfirmCode(getMin(duration), getSec(duration))
        _stateUI.value = RepeatPasswordTimer(time, resourceProvider.getTitleInputTimerSpan())
    }

    private fun getMin(duration: Int): Int {
        return duration / TIME_DIVIDER
    }

    private fun getSec(duration: Int): Int {
        return duration % TIME_DIVIDER
    }

    override fun onTimeIsOverState() {
        _stateUI.value = RepeatPassword
    }

    companion object {
        const val TIME_DIVIDER = 60
    }

}