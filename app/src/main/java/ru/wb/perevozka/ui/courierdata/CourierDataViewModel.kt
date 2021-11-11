package ru.wb.perevozka.ui.courierdata

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
import ru.wb.perevozka.ui.courierdata.domain.CourierDataInteractor
import ru.wb.perevozka.ui.dialogs.DialogInfoStyle
import ru.wb.perevozka.ui.dialogs.NavigateToDialogInfo
import ru.wb.perevozka.utils.LogUtils
import java.util.*

class UserFormViewModel(
    private val parameters: CourierDataParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierDataInteractor,
    private val resourceProvider: CourierDataResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToMessageInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToMessageInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigationEvent =
        SingleLiveEvent<CourierDataNavAction>()
    val navigationEvent: LiveData<CourierDataNavAction>
        get() = _navigationEvent

    private val _formUIState = MutableLiveData<CourierDataUIState>()
    val formUIState: LiveData<CourierDataUIState>
        get() = _formUIState

    private val _loaderState = MutableLiveData<CourierDataUILoaderState>()
    val loaderState: LiveData<CourierDataUILoaderState>
        get() = _loaderState

    init {
        observeNetworkState()
    }

    private fun checkFocusSurnameWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkSurname(focusChange.text, focusChange.type)
    }

    private fun checkTextSurnameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkSurname(focusChange.text, focusChange.type)
    }

    private fun checkSurname(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.isEmpty()) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusNameWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkName(focusChange.text, focusChange.type)
    }

    private fun checkTextNameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkName(focusChange.text, focusChange.type)
    }

    private fun checkName(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.isEmpty()) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusMiddleNameWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkMiddleName(focusChange.text, focusChange.type)
    }

    private fun checkTextMiddleNameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkMiddleName(focusChange.text, focusChange.type)
    }

    private fun checkMiddleName(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.isEmpty()) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusInnWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkInn(focusChange.text, focusChange.type)
    }

    private fun checkTextInnWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkInn(focusChange.text, focusChange.type)
    }

    private fun checkInn(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.length != 12) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportSeriesWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkPassportSeries(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportSeriesWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkPassportSeries(focusChange.text, focusChange.type)
    }

    private fun checkPassportSeries(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.length != 4) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportNumberWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkPassportNumber(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportNumberWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkPassportNumber(focusChange.text, focusChange.type)
    }

    private fun checkPassportNumber(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.length != 6) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportDateWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkPassportDate(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportDateWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkPassportDate(focusChange.text, focusChange.type)
    }

    private fun checkPassportDate(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.length != 10) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportCodeWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkPassportCode(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportCodeWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkPassportCode(focusChange.text, focusChange.type)
    }

    private fun checkPassportCode(text: String, type: CourierDataQueryType): CourierDataUIState {
        return if (text.length != 6) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportIssuedByWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkPassportIssuedBy(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportIssuedByWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkPassportIssuedBy(focusChange.text, focusChange.type)
    }

    private fun checkPassportIssuedBy(
        text: String,
        type: CourierDataQueryType
    ): CourierDataUIState {
        return if (text.isEmpty()) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    private fun checkFocusPassportDepartmentCodeWrapper(focusChange: CourierDataUIAction.FocusChange): CourierDataUIState {
        return checkPassportDepartmentCode(focusChange.text, focusChange.type)
    }

    private fun checkTextPassportDepartmentCodeWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState {
        return checkPassportDepartmentCode(focusChange.text, focusChange.type)
    }

    private fun checkPassportDepartmentCode(
        text: String,
        type: CourierDataQueryType
    ): CourierDataUIState {
        return if (text.length != 6) {
            CourierDataUIState.Error(text, type)
        } else {
            CourierDataUIState.Complete(text, type)
        }
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierDataUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .map { mapAction(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapAction(action: CourierDataUIAction) = when (action) {
        is CourierDataUIAction.FocusChange -> checkFieldFocus(action)
        is CourierDataUIAction.TextChange ->  checkFieldText(action)
        is CourierDataUIAction.CompleteClick -> checkFieldAll(action)
    }

    private fun checkFieldAll(action: CourierDataUIAction.CompleteClick): CourierDataUIState {
        val iterator = action.userData.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            when (item.type) {
                CourierDataQueryType.SURNAME -> if (isNotCheck(
                        checkSurname(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.NAME -> if (isNotCheck(
                        checkName(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.MIDDLE_NAME -> if (isNotCheck(
                        checkMiddleName(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.INN -> if (isNotCheck(
                        checkInn(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.PASSPORT_SERIES -> if (isNotCheck(
                        checkPassportSeries(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.PASSPORT_NUMBER -> if (isNotCheck(
                        checkPassportNumber(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.PASSPORT_DATE -> if (isNotCheck(
                        checkPassportDate(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.PASSPORT_CODE -> if (isNotCheck(
                        checkPassportCode(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.PASSPORT_ISSUED_BY -> if (isNotCheck(
                        checkPassportIssuedBy(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
                CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> if (isNotCheck(
                        checkPassportDepartmentCode(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
            }
        }
        return if (action.userData.isEmpty()) {
            // TODO: 20.08.2021 выполнить загрузку данных пользователя
            CourierDataUIState.Next
        } else {
            CourierDataUIState.ErrorFocus("", action.userData.first().type)
        }
    }

    private fun checkFieldFocus(action: CourierDataUIAction.FocusChange) =
        when (action.type) {
            CourierDataQueryType.SURNAME -> checkFocusSurnameWrapper(action)
            CourierDataQueryType.NAME -> checkFocusNameWrapper(action)
            CourierDataQueryType.MIDDLE_NAME -> checkFocusMiddleNameWrapper(action)
            CourierDataQueryType.INN -> checkFocusInnWrapper(action)
            CourierDataQueryType.PASSPORT_SERIES -> checkFocusPassportSeriesWrapper(action)
            CourierDataQueryType.PASSPORT_NUMBER -> checkFocusPassportNumberWrapper(action)
            CourierDataQueryType.PASSPORT_DATE -> checkFocusPassportDateWrapper(action)
            CourierDataQueryType.PASSPORT_CODE -> checkFocusPassportCodeWrapper(action)
            CourierDataQueryType.PASSPORT_ISSUED_BY -> checkFocusPassportIssuedByWrapper(action)
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> checkFocusPassportDepartmentCodeWrapper(
                action
            )
        }

    private fun checkFieldText(action: CourierDataUIAction.TextChange) =
        when (action.type) {
            CourierDataQueryType.SURNAME -> checkTextSurnameWrapper(action)
            CourierDataQueryType.NAME -> checkTextNameWrapper(action)
            CourierDataQueryType.MIDDLE_NAME -> checkTextMiddleNameWrapper(action)
            CourierDataQueryType.INN -> checkTextInnWrapper(action)
            CourierDataQueryType.PASSPORT_SERIES -> checkTextPassportSeriesWrapper(action)
            CourierDataQueryType.PASSPORT_NUMBER -> checkTextPassportNumberWrapper(action)
            CourierDataQueryType.PASSPORT_DATE -> checkTextPassportDateWrapper(action)
            CourierDataQueryType.PASSPORT_CODE -> checkTextPassportCodeWrapper(action)
            CourierDataQueryType.PASSPORT_ISSUED_BY -> checkTextPassportIssuedByWrapper(action)
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> checkTextPassportDepartmentCodeWrapper(
                action
            )
        }

    private fun isNotCheck(state: CourierDataUIState) = state is CourierDataUIState.Complete

    fun onNextClick(courierDocumentsEntity: CourierDocumentsEntity) {
        _loaderState.value = CourierDataUILoaderState.Progress
        addSubscription(
            interactor.courierDocuments(courierDocumentsEntity).subscribe(
                { couriersFormComplete() },
                { couriersFormError(it) })
        )
    }

    fun onCheckedClick(isComplete: Boolean, isAgreement: Boolean, isPersonal: Boolean) {
        _loaderState.value = if (isComplete && isAgreement && isPersonal) {
            CourierDataUILoaderState.Enable
        } else {
            CourierDataUILoaderState.Disable
        }
    }

    private fun couriersFormComplete() {
        _loaderState.value = CourierDataUILoaderState.Disable
        _navigationEvent.value =
            CourierDataNavAction.NavigateToCouriersCompleteRegistration(parameters.phone)
    }

    private fun couriersFormError(throwable: Throwable) {
        val message = when (throwable) {

            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.INFO.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.INFO.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _loaderState.value = CourierDataUILoaderState.Enable
        _navigateToMessageInfo.value = message

    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    fun onShowAgreementClick() {
        _navigationEvent.value = CourierDataNavAction.NavigateToAgreement
    }

}