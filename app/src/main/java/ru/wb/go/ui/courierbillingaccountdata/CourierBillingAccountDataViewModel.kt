package ru.wb.go.ui.courierbillingaccountdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbillingaccountdata.domain.CourierBillingAccountDataInteractor
import ru.wb.go.ui.courierdata.Message
import ru.wb.go.ui.dialogs.DialogStyle
import ru.wb.go.utils.LogUtils
import java.util.*

class CourierBillingAccountDataViewModel(
    private val parameters: CourierBillingAccountDataAmountParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierBillingAccountDataInteractor,
    private val resourceProvider: CourierBillingAccountDataResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    companion object {
        private const val INN_LENGTH = 12
        private const val ACCOUNT_LENGTH = 20
        private const val BIK_LENGTH = 9
        private const val KPP_LENGTH = 9
        private const val COR_ACCOUNT_LENGTH = 20
        private const val INN_BANK_LENGTH = 10
    }

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _initUIState = MutableLiveData<CourierBillingAccountDataInitUIState>()
    val initUIState: LiveData<CourierBillingAccountDataInitUIState>
        get() = _initUIState

    private val _navigateToMessageState = SingleLiveEvent<Message>()
    val navigateToMessageState: LiveData<Message>
        get() = _navigateToMessageState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigationEvent =
        SingleLiveEvent<CourierBillingAccountDataNavAction>()
    val navigationEvent: LiveData<CourierBillingAccountDataNavAction>
        get() = _navigationEvent

    private val _formUIState = MutableLiveData<CourierBillingAccountDataUIState>()
    val formUIState: LiveData<CourierBillingAccountDataUIState>
        get() = _formUIState

    private val _loaderState = MutableLiveData<CourierBillingAccountDataUILoaderState>()
    val loaderState: LiveData<CourierBillingAccountDataUILoaderState>
        get() = _loaderState

    init {
        initToolbarLabel()
        observeNetworkState()
        initStateField()
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value =
            if (parameters.account.isEmpty()) resourceProvider.getTitleCreate() else resourceProvider.getTitleEdit()
    }

    private fun initStateField() {
        if (parameters.account.isEmpty()) _initUIState.value =
            CourierBillingAccountDataInitUIState.Create else {
            addSubscription(
                interactor.getAccount(parameters.account)
                    .subscribe(
                        { _initUIState.value = CourierBillingAccountDataInitUIState.Edit(it) },
                        { LogUtils { logDebugApp(it.toString()) } }) //_initUIState.value =CourierBillingAccountDataInitUIState.Create
            )
        }
    }

    private fun predicateChecker(
        predicate: (String) -> Boolean,
        text: String,
        type: CourierBillingAccountDataQueryType
    ): CourierBillingAccountDataUIState {
        return if (predicate(text))
            CourierBillingAccountDataUIState.Error(text, type)
        else CourierBillingAccountDataUIState.Complete(text, type)
    }

    private fun isCompleteChecker(
        predicate: (String) -> Boolean,
        text: String,
        type: CourierBillingAccountDataQueryType
    ) = (predicateChecker(predicate, text, type)) is CourierBillingAccountDataUIState.Complete


    private val surnamePredicate = { text: String -> text.isEmpty() }

    private fun checkFocusSurnameWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(surnamePredicate, text, type) }
    }

    private fun checkTextSurnameWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(surnamePredicate, text, type) }
    }

    private val namePredicate = { text: String -> text.isEmpty() }

    private fun checkFocusNameWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(namePredicate, text, type) }
    }

    private fun checkTextNameWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(namePredicate, text, type) }
    }

    private val middleNamePredicate = { text: String -> text.isEmpty() }

    private fun checkFocusMiddleNameWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(middleNamePredicate, text, type) }
    }

    private fun checkTextMiddleNameWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(middleNamePredicate, text, type) }
    }

    private val innPredicate = { text: String -> text.length != INN_LENGTH }

    private fun checkFocusInnWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(innPredicate, text, type) }
    }

    private fun checkTextInnWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(innPredicate, text, type) }
    }

    private val accountPredicate = { text: String -> text.length != ACCOUNT_LENGTH }

    private fun checkFocusAccountWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(accountPredicate, text, type) }
    }

    private fun checkTextAccountWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(accountPredicate, text, type) }
    }

    private val bankPredicate = { text: String -> text.isEmpty() }

    private fun checkFocusBankWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(bankPredicate, text, type) }
    }

    private fun checkTextBankWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(bankPredicate, text, type) }
    }

    private val bikPredicate = { text: String -> text.length != KPP_LENGTH }

    private fun checkFocusBikWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(bikPredicate, text, type) }
    }

    private fun checkTextBikWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(bikPredicate, text, type) }
    }

    private val kppPredicate = { text: String -> text.length != BIK_LENGTH }

    private fun checkFocusKppWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(kppPredicate, text, type) }
    }

    private fun checkTextKppWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(kppPredicate, text, type) }
    }

    private val corAccountPredicate = { text: String -> text.length != COR_ACCOUNT_LENGTH }

    private fun checkFocusCorAccountWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(corAccountPredicate, text, type) }
    }

    private fun checkTextCorAccountWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(corAccountPredicate, text, type) }
    }

    private val innBankPredicate = { text: String -> text.length != INN_BANK_LENGTH }

    private fun checkFocusInnBankWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(innBankPredicate, text, type) }
    }

    private fun checkTextInnBankWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
        return with(focusChange) { predicateChecker(innBankPredicate, text, type) }
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierBillingAccountDataUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .map { mapAction(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapAction(action: CourierBillingAccountDataUIAction) = when (action) {
        is CourierBillingAccountDataUIAction.FocusChange -> checkFieldFocus(action)
        is CourierBillingAccountDataUIAction.TextChange -> checkFieldText(action)
        is CourierBillingAccountDataUIAction.CompleteClick -> checkFieldAll(action)
    }

    private fun checkFieldFocus(action: CourierBillingAccountDataUIAction.FocusChange) =
        when (action.type) {
            CourierBillingAccountDataQueryType.SURNAME -> checkFocusSurnameWrapper(action)
            CourierBillingAccountDataQueryType.NAME -> checkFocusNameWrapper(action)
            CourierBillingAccountDataQueryType.MIDDLE_NAME -> checkFocusMiddleNameWrapper(action)
            CourierBillingAccountDataQueryType.INN -> checkFocusInnWrapper(action)
            CourierBillingAccountDataQueryType.ACCOUNT -> checkFocusAccountWrapper(action)
            CourierBillingAccountDataQueryType.BANK -> checkFocusBankWrapper(action)
            CourierBillingAccountDataQueryType.BIK -> checkFocusBikWrapper(action)
            CourierBillingAccountDataQueryType.KPP -> checkFocusKppWrapper(action)
            CourierBillingAccountDataQueryType.COR_ACCOUNT -> checkFocusCorAccountWrapper(action)
            CourierBillingAccountDataQueryType.INN_BANK -> checkFocusInnBankWrapper(action)
        }

    private fun checkFieldText(action: CourierBillingAccountDataUIAction.TextChange) =
        when (action.type) {
            CourierBillingAccountDataQueryType.SURNAME -> checkTextSurnameWrapper(action)
            CourierBillingAccountDataQueryType.NAME -> checkTextNameWrapper(action)
            CourierBillingAccountDataQueryType.MIDDLE_NAME -> checkTextMiddleNameWrapper(action)
            CourierBillingAccountDataQueryType.INN -> checkTextInnWrapper(action)
            CourierBillingAccountDataQueryType.ACCOUNT -> checkTextAccountWrapper(action)
            CourierBillingAccountDataQueryType.BANK -> checkTextBankWrapper(action)
            CourierBillingAccountDataQueryType.BIK -> checkTextBikWrapper(action)
            CourierBillingAccountDataQueryType.KPP -> checkTextKppWrapper(action)
            CourierBillingAccountDataQueryType.COR_ACCOUNT -> checkTextCorAccountWrapper(action)
            CourierBillingAccountDataQueryType.INN_BANK -> checkTextInnBankWrapper(action)
        }

    private fun checkFieldAll(action: CourierBillingAccountDataUIAction.CompleteClick): CourierBillingAccountDataUIState {
        val iterator = action.userData.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            with(item) {
                val predicate = when (type) {
                    CourierBillingAccountDataQueryType.SURNAME -> surnamePredicate
                    CourierBillingAccountDataQueryType.NAME -> namePredicate
                    CourierBillingAccountDataQueryType.MIDDLE_NAME -> middleNamePredicate
                    CourierBillingAccountDataQueryType.INN -> innPredicate
                    CourierBillingAccountDataQueryType.ACCOUNT -> accountPredicate
                    CourierBillingAccountDataQueryType.BANK -> bankPredicate
                    CourierBillingAccountDataQueryType.BIK -> bikPredicate
                    CourierBillingAccountDataQueryType.KPP -> kppPredicate
                    CourierBillingAccountDataQueryType.COR_ACCOUNT -> corAccountPredicate
                    CourierBillingAccountDataQueryType.INN_BANK -> innBankPredicate
                }
                if (isCompleteChecker(predicate, text, type)) iterator.remove()
            }
        }
        return if (action.userData.isEmpty()) {
            // TODO: 20.08.2021 выполнить загрузку данных пользователя
            CourierBillingAccountDataUIState.Next
        } else {
            CourierBillingAccountDataUIState.ErrorFocus("", action.userData.first().type)
        }
    }

    fun onSaveClick(courierDocumentsEntity: CourierBillingAccountEntity) {
        saveAccount(courierDocumentsEntity)
    }

    private fun saveAccount(courierDocumentsEntity: CourierBillingAccountEntity) {
        _loaderState.value = CourierBillingAccountDataUILoaderState.Progress
        addSubscription(
            interactor.saveAccount(courierDocumentsEntity).subscribe(
                { saveAccountComplete() },
                { saveAccountError(it) })
        )
    }

    private fun saveAccountComplete() {
        _loaderState.value = CourierBillingAccountDataUILoaderState.Disable
        _navigationEvent.value =
            CourierBillingAccountDataNavAction.NavigateToAccountSelector(parameters.amount)
    }

    private fun saveAccountError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> Message(
                DialogStyle.INFO.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> Message(
                DialogStyle.INFO.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> Message(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _loaderState.value = CourierBillingAccountDataUILoaderState.Enable
        _navigateToMessageState.value = message
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    fun onRemoveAccountClick() {

    }

    fun onSaveChangeAccountClick(courierDocumentsEntity: CourierBillingAccountEntity) {
        saveAccount(courierDocumentsEntity)
    }

}