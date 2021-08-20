package ru.wb.perevozka.ui.userdata.userform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.userdata.UserDataResourceProvider
import ru.wb.perevozka.ui.userdata.userform.domain.UserFormInteractor
import ru.wb.perevozka.utils.LogUtils
import java.util.*

class UserFormViewModel(
    private val parameters: UserFormParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: UserFormInteractor,
    private val resourceProvider: UserDataResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigationEvent =
        SingleLiveEvent<UserFormNavAction>()
    val navigationEvent: LiveData<UserFormNavAction>
        get() = _navigationEvent

    private val _formUIState = MutableLiveData<UserFormUIState>()
    val formUIState: LiveData<UserFormUIState>
        get() = _formUIState

    private val _loaderState = MutableLiveData<UserFormUILoaderState>()
    val loaderState: LiveData<UserFormUILoaderState>
        get() = _loaderState

    init {
        observeNetworkState()
    }

    private fun checkFocusSurnameWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkSurname(focusChange.text, focusChange.type)
    }

    private fun checkTextSurnameWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkSurname(focusChange.text, focusChange.type)
    }

    private fun checkSurname(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.isEmpty()) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusNameWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkName(focusChange.text, focusChange.type)
    }

    private fun checkTextNameWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkName(focusChange.text, focusChange.type)
    }

    private fun checkName(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.isEmpty()) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusMiddleNameWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkMiddleName(focusChange.text, focusChange.type)
    }

    private fun checkTextMiddleNameWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkMiddleName(focusChange.text, focusChange.type)
    }

    private fun checkMiddleName(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.isEmpty()) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusInnWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkInn(focusChange.text, focusChange.type)
    }

    private fun checkTextInnWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkInn(focusChange.text, focusChange.type)
    }

    private fun checkInn(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.length != 12) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportSeriesWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkPassportSeries(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportSeriesWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkPassportSeries(focusChange.text, focusChange.type)
    }

    private fun checkPassportSeries(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.length != 4) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportNumberWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkPassportNumber(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportNumberWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkPassportNumber(focusChange.text, focusChange.type)
    }

    private fun checkPassportNumber(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.length != 6) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportDateWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkPassportDate(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportDateWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkPassportDate(focusChange.text, focusChange.type)
    }

    private fun checkPassportDate(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.length != 10) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportCodeWrapper(focusChange: UserFormUIAction.FocusChange): UserFormUIState {
        return checkPassportCode(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportCodeWrapper(focusChange: UserFormUIAction.TextChange): UserFormUIState {
        return checkPassportCode(focusChange.text, focusChange.type)
    }

    private fun checkPassportCode(text: String, type: UserFormQueryType): UserFormUIState {
        return if (text.length != 6) {
            UserFormUIState.Error(text, type)
        } else {
            UserFormUIState.Complete(text, type)
        }
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<UserFormUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .map { mapAction(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapAction(action: UserFormUIAction) = when (action) {
        is UserFormUIAction.FocusChange -> checkFocus(action)
        is UserFormUIAction.TextChange -> checkText(action)
        is UserFormUIAction.CompleteClick -> checkAll(action)
    }

    private fun checkAll(action: UserFormUIAction.CompleteClick): UserFormUIState {
        val iterator = action.userData.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            when (item.type) {
                UserFormQueryType.SURNAME -> if (isNotCheck(
                        checkSurname(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.NAME -> if (isNotCheck(
                        checkName(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.MIDDLE_NAME -> if (isNotCheck(
                        checkMiddleName(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.INN -> if (isNotCheck(
                        checkInn(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.PASSPORT_SERIES -> if (isNotCheck(
                        checkPassportSeries(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.PASSPORT_NUMBER -> if (isNotCheck(
                        checkPassportNumber(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.PASSPORT_DATE -> if (isNotCheck(
                        checkPassportDate(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                UserFormQueryType.PASSPORT_CODE -> if (isNotCheck(
                        checkPassportCode(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
            }
        }
        return if (action.userData.isEmpty()) {
            // TODO: 20.08.2021 выполнить загрузку данных пользователя
            UserFormUIState.Next
        } else {
            UserFormUIState.ErrorFocus("", action.userData.first().type)
        }
    }

    private fun checkFocus(action: UserFormUIAction.FocusChange) =
        when (action.type) {
            UserFormQueryType.SURNAME -> checkFocusSurnameWrapper(action)
            UserFormQueryType.NAME -> checkFocusNameWrapper(action)
            UserFormQueryType.MIDDLE_NAME -> checkFocusMiddleNameWrapper(action)
            UserFormQueryType.INN -> checkFocusInnWrapper(action)
            UserFormQueryType.PASSPORT_SERIES -> checkFocusPassportSeriesWrapper(action)
            UserFormQueryType.PASSPORT_NUMBER -> checkFocusPassportNumberWrapper(action)
            UserFormQueryType.PASSPORT_DATE -> checkFocusPassportDateWrapper(action)
            UserFormQueryType.PASSPORT_CODE -> checkFocusPassportCodeWrapper(action)
        }

    private fun checkText(action: UserFormUIAction.TextChange) =
        when (action.type) {
            UserFormQueryType.SURNAME -> checkTextSurnameWrapper(action)
            UserFormQueryType.NAME -> checkTextNameWrapper(action)
            UserFormQueryType.MIDDLE_NAME -> checkTextMiddleNameWrapper(action)
            UserFormQueryType.INN -> checkTextInnWrapper(action)
            UserFormQueryType.PASSPORT_SERIES -> checkTextPassportSeriesWrapper(action)
            UserFormQueryType.PASSPORT_NUMBER -> checkTextPassportNumberWrapper(action)
            UserFormQueryType.PASSPORT_DATE -> checkTextPassportDateWrapper(action)
            UserFormQueryType.PASSPORT_CODE -> checkTextPassportCodeWrapper(action)
        }

    private fun isNotCheck(state: UserFormUIState) = state is UserFormUIState.Complete

    fun onNextClick(courierDocumentsEntity: CourierDocumentsEntity) {
        _loaderState.value = UserFormUILoaderState.Progress
        addSubscription(
            interactor.courierDocuments(courierDocumentsEntity).subscribe(
                { couriersFormComplete() },
                { couriersFormError(it) })
        )
    }

    fun onCheckedClick(isComplete: Boolean, isPersonal: Boolean) {
        _loaderState.value = if (isComplete && isPersonal) {
            UserFormUILoaderState.Enable
        } else {
            UserFormUILoaderState.Disable
        }
    }

    private fun couriersFormComplete() {
        _loaderState.value = UserFormUILoaderState.Disable
        _navigationEvent.value =
            UserFormNavAction.NavigateToCouriersCompleteRegistration(parameters.phone)
    }

    private fun couriersFormError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getGenericError()
        }
        _loaderState.value = UserFormUILoaderState.Enable
        _navigateToMessageInfo.value =
            NavigateToMessageInfo("Отправка данных", message, "OK")
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    data class NavigateToMessageInfo(
        val title: String,
        val message: String,
        val button: String
    )

}