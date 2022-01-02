package ru.wb.go.ui.courierdata


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.mvvm.BaseMessageResourceProvider
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierdata.domain.CourierDataInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.analytics.YandexMetricManager
import java.util.*

class UserFormViewModel(
    private val parameters: CourierDataParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierDataInteractor,
    private val resourceProvider: BaseMessageResourceProvider,

    ) : NetworkViewModel(compositeDisposable, metric) {

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToMessageInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToMessageInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

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


    private fun checkTextSurnameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)


    private fun checkInputRequisite(field: CourierDataUIAction.TextChange): CourierDataUIState {
        val pattern = when (field.type) {
            CourierDataQueryType.MIDDLE_NAME -> """^\$|[а-яА-ЯёЁ -.`']+$"""
            CourierDataQueryType.INN -> """^\d{12}$"""
            CourierDataQueryType.PASSPORT_SERIES -> """^\d{4}$"""
            CourierDataQueryType.PASSPORT_NUMBER -> """^\d{6}$"""
            CourierDataQueryType.PASSPORT_DATE -> """^.+$"""
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> """^\d{6}$"""
            CourierDataQueryType.PASSPORT_ISSUED_BY -> """^.+$"""
            else -> """^[а-яА-ЯёЁ -.`']+$"""
        }
        val regex = Regex(pattern)

        return if (!regex.matches(field.text)) {
            CourierDataUIState.Error(field.text, field.type)
        } else {
            CourierDataUIState.Complete(field.text, field.type)
        }
    }

    private fun checkTextNameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextMiddleNameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextInnWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportSeriesWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportNumberWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportDateWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportIssuedByWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportDepartmentCodeWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierDataUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .map { mapAction(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapAction(action: CourierDataUIAction) = when (action) {
        is CourierDataUIAction.TextChange -> checkFieldText(action)
        is CourierDataUIAction.CompleteClick -> checkFieldAll(action)
    }

    private fun checkFieldAll(action: CourierDataUIAction.CompleteClick): CourierDataUIState {
        val iterator = action.userData.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            val field = CourierDataUIAction.TextChange(item.text, item.type)
            if (isNotCheck(checkInputRequisite(field))) {
                return CourierDataUIState.ErrorFocus("", item.type)
            }

            iterator.remove()
        }
        return CourierDataUIState.Next

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
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> checkTextPassportDepartmentCodeWrapper(
                action
            )
            CourierDataQueryType.PASSPORT_ISSUED_BY -> checkTextPassportIssuedByWrapper(action)
        }

    private fun isNotCheck(state: CourierDataUIState) = state !is CourierDataUIState.Complete

    fun onNextClick(courierDocumentsEntity: CourierDocumentsEntity) {
        _loaderState.value = CourierDataUILoaderState.Progress
        addSubscription(
            interactor.saveCourierDocuments(courierDocumentsEntity).subscribe(
                { couriersFormComplete() },
                { couriersFormError(it) })
        )
    }

    fun onCheckedClick(isAgreement: Boolean) {
        _loaderState.value = if (isAgreement) {
            CourierDataUILoaderState.Enable
        } else {
            CourierDataUILoaderState.Disable
        }
    }

    private fun couriersFormComplete() {
        onTechEventLog("couriersFormComplete", "NavigateToCouriersCompleteRegistration")
        _loaderState.value = CourierDataUILoaderState.Disable
        _navigationEvent.value =
            CourierDataNavAction.NavigateToCouriersCompleteRegistration(parameters.phone)
    }

    private fun couriersFormError(throwable: Throwable) {
        onTechErrorLog("couriersFormError", throwable)
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.INFO.ordinal,
                resourceProvider.getGenericInternetTitleError(),
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.INFO.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.error.message,
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
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
        onTechEventLog("onShowAgreementClick")
        _navigationEvent.value = CourierDataNavAction.NavigateToAgreement
    }

    fun getDocsParam():CourierDocumentsEntity{
        return parameters.docs
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "UserForm"
    }


}