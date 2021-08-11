package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.domain.CheckSmsData
import ru.wb.perevozka.ui.auth.domain.CheckSmsInteractor
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateHandler

class CheckSmsViewModel(
    private val parameters: CheckSmsParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CheckSmsInteractor,
    private val resourceProvider: AuthResourceProvider,
) : TimerStateHandler, NetworkViewModel(compositeDisposable) {

    private val _stateTitleUI = MutableLiveData<InitTitle>()
    val stateTitleUI: LiveData<InitTitle>
        get() = _stateTitleUI

    private val _navigationEvent =
        SingleLiveEvent<CheckSmsNavAction>()
    val navigationEvent: LiveData<CheckSmsNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _stateUI = MutableLiveData<CheckSmsUIState>()
    val stateUI: LiveData<CheckSmsUIState>
        get() = _stateUI

    private val _repeatStateUI = MutableLiveData<CheckSmsUIRepeatState>()
    val repeatStateUI: LiveData<CheckSmsUIRepeatState>
        get() = _repeatStateUI

    init {
        observeNetworkState()
        fetchTitle()
        subscribeTimer()
        restartTimer(if (parameters.ttl > 0) parameters.ttl else DURATION_TIME_INIT)
    }

    private fun fetchTitle() {
        _stateTitleUI.postValue(
            InitTitle(resourceProvider.getTitleCheckSms(parameters.phone), parameters.phone)
        )
    }

    fun passwordChanges(observable: InitialValueObservable<CharSequence>) {
        fetchPasswordChanges(observable)
    }

    fun authClick(password: String) {
        fetchAuth(password)
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

    fun onRepeatPassword() {
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordProgress
        addSubscription(
            interactor.couriersExistAndSavePhone(formatPhone()).subscribe(
                { fetchingPasswordComplete() },
                { fetchingPasswordError(it) }
            )
        )
    }

    private fun fetchingPasswordComplete() {
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordComplete
    }

    private fun fetchingPasswordError(throwable: Throwable) {
        when (throwable) {
            is NoInternetException -> _repeatStateUI.value =
                CheckSmsUIRepeatState.ErrorPassword(throwable.message)
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
                CheckSmsUIRepeatState.ErrorPassword(resourceProvider.getGenericError())
        }
    }

    private fun restartTimer(durationTime: Int) {
        interactor.startTimer(durationTime)
    }

    private fun fetchPasswordChanges(observable: InitialValueObservable<CharSequence>) {
        addSubscription(
            interactor.remindPasswordChanges(observable)
                .subscribe(
                    {
                        _stateUI.value =
                            if (it) CheckSmsUIState.SaveAndNextEnable else CheckSmsUIState.SaveAndNextDisable
                    },
                    { _stateUI.value = CheckSmsUIState.SaveAndNextDisable }
                )
        )
    }

    private fun fetchAuth(password: String) {
        _stateUI.value = CheckSmsUIState.Progress
        val phone = formatPhone()
        addSubscription(interactor.auth(phone, password)
            .map { checkSmsData(it, phone) }
            .subscribe(
                { authComplete(it) },
                { authError(it) }
            )
        )
    }

    private fun checkSmsData(it: CheckSmsData, phone: String) = when (it) {
        CheckSmsData.NeedApproveCourierDocuments ->
            CheckSmsNavAction.NavigateToCompletionRegistration(phone)
        CheckSmsData.NeedSendCourierDocument ->
            CheckSmsNavAction.NavigateToUserForm(phone)
        CheckSmsData.UserRegistered ->
            CheckSmsNavAction.NavigateToApplication
    }

    private fun formatPhone() = parameters.phone.filter { it.isDigit() }

    private fun authComplete(createPasswordNavAction: CheckSmsNavAction) {
        _stateUI.value = CheckSmsUIState.Complete
        _navigationEvent.value = createPasswordNavAction
    }

    private fun authError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> CheckSmsUIState.MessageError(throwable.message)
            is BadRequestException -> CheckSmsUIState.Error //throwable.error.message
            else -> CheckSmsUIState.MessageError(resourceProvider.getGenericError())
        }
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    data class InitTitle(val title: String, val phone: String)

    override fun onTimerState(duration: Int) {
        val time: String =
            resourceProvider.getSignUpTimeConfirmCode(getMin(duration), getSec(duration))
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordTimer(
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
        _repeatStateUI.value = CheckSmsUIRepeatState.RepeatPasswordComplete
    }

    companion object {
        private const val DURATION_TIME_INIT = 180
        const val TIME_DIVIDER = 60
    }

}