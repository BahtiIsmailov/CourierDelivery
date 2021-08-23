package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.domain.CheckSmsData
import ru.wb.perevozka.ui.auth.domain.CheckSmsInteractor
import ru.wb.perevozka.ui.auth.keyboard.KeyboardNumericView
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

    private val _stateKeyboardBackspaceUI = SingleLiveEvent<CheckSmsBackspaceUIState>()
    val stateBackspaceUI: LiveData<CheckSmsBackspaceUIState>
        get() = _stateKeyboardBackspaceUI

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

    fun onNumberObservableClicked(event: Observable<KeyboardNumericView.ButtonAction>) {
        addSubscription(
            event.scan(String(), { accumulator, item -> accumulateCode(accumulator, item) })
                .doOnNext { switchNext(it) }
                .subscribe(
                    { formatSmsComplete(it) },
                    { })
        )
    }

    private fun formatSmsComplete(code: String) {
        _stateUI.value = CheckSmsUIState.CodeFormat(code)
        if (code.length == NUMBER_LENGTH_MAX) fetchAuth(code)
    }

    private fun checkSmsError(throwable: Throwable) {
        _stateUI.value = when (throwable) {
            is NoInternetException -> CheckSmsUIState.MessageError(
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> CheckSmsUIState.Error
            else -> CheckSmsUIState.MessageError(
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
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
                CheckSmsUIRepeatState.ErrorPassword(
                    throwable.message,
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
                    resourceProvider.getGenericServiceMessageError(),
                    resourceProvider.getGenericServiceButtonError()
                )
        }
    }

    private fun restartTimer(durationTime: Int) {
        interactor.startTimer(durationTime)
    }

    private fun fetchAuth(password: String) {
        _stateUI.value = CheckSmsUIState.Progress
        val phone = formatPhone()
        addSubscription(interactor.auth(phone, password)
            .map { checkSmsData(it, phone) }
            .subscribe(
                { authComplete(it) },
                { checkSmsError(it) }
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

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

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

    override fun onCleared() {
        super.onCleared()
        interactor.stopTimer()
    }

    companion object {
        private const val DURATION_TIME_INIT = 180
        const val TIME_DIVIDER = 60
        const val NUMBER_LENGTH_MAX = 4
    }

    data class InitTitle(val title: String, val phone: String)

}