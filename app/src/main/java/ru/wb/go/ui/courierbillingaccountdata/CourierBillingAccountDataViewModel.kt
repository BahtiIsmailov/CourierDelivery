package ru.wb.go.ui.courierbillingaccountdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.entity.convertToCourierBillingAccountEditableEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbillingaccountdata.domain.CourierBillingAccountDataInteractor
import ru.wb.go.ui.courierbillingaccountdata.domain.EditableResult
import ru.wb.go.ui.courierdata.Message
import ru.wb.go.ui.dialogs.DialogStyle
import ru.wb.go.utils.LogUtils
import java.util.*

class CourierBillingAccountDataViewModel(
    private val parameters: CourierBillingAccountDataAmountParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierBillingAccountDataInteractor,
    private val resourceProvider: CourierBillingAccountDataResourceProvider,
    private val tokenManager: TokenManager
) : NetworkViewModel(compositeDisposable) {

    companion object {
        private const val ACCOUNT_LENGTH = 20
        private const val KPP_LENGTH = 9
        private val PREFIX_ACCOUNT = listOf("40702", "40802", "40817")
    }

    private var bankEntity: BankEntity? = null

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

    private val _bicProgressState = MutableLiveData<Boolean>()
    val bicProgressState: LiveData<Boolean>
        get() = _bicProgressState

    private val _keyboardState = MutableLiveData<Boolean>()
    val keyboardState: LiveData<Boolean>
        get() = _keyboardState

    private val _bankNameState = MutableLiveData<String>()
    val bankNameState: LiveData<String>
        get() = _bankNameState

    private val _loaderState = MutableLiveData<CourierBillingAccountDataUILoaderState>()
    val loaderState: LiveData<CourierBillingAccountDataUILoaderState>
        get() = _loaderState

    private val _holderState = MutableLiveData<Boolean>()
    val holderState: LiveData<Boolean>
        get() = _holderState

    init {
        initToolbarLabel()
        observeNetworkState()
        initStateField()
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value =
            if (parameters.account.isEmpty()) resourceProvider.getTitleCreate()
            else resourceProvider.getTitleEdit()
    }

    private fun initStateField() {
        if (parameters.account.isEmpty()) createAccount()
        else editAccount()
    }

    private fun createAccount() {
        _initUIState.value = CourierBillingAccountDataInitUIState.Create(
            tokenManager.userName(),
            tokenManager.userInn()
        )
    }

    private fun editAccount() {
        addSubscription(
            interactor.getEditableResult(parameters.account)
                .map { convertToEditState(it) }
                .subscribe(
                    { _initUIState.value = it },
                    { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun convertToEditState(it: EditableResult) =
        with(it) {
            CourierBillingAccountDataInitUIState.Edit(
                courierBillingAccountEntity.convertToCourierBillingAccountEditableEntity(),
                isRemovable
            )
        }

    private fun predicateMessageChecker(
        predicate: (String) -> String,
        text: String,
        type: CourierBillingAccountDataQueryType
    ): CourierBillingAccountDataUIState {
        val message = predicate(text)
        return if (message.isEmpty())
            CourierBillingAccountDataUIState.Complete(text, type)
        else CourierBillingAccountDataUIState.Error(message, type)
    }

    private fun isCompleteChecker(
        predicate: (String) -> String,
        text: String,
        type: CourierBillingAccountDataQueryType
    ) = (predicateMessageChecker(
        predicate,
        text,
        type
    )) is CourierBillingAccountDataUIState.Complete

    private val accountPredicate = { text: String ->
        var isPrefix = false
        PREFIX_ACCOUNT.forEach { prefix ->
            if (prefix.length >= text.length) {
                if (prefix.substring(0, text.length) == text) isPrefix = true
            } else {
                if (text.substring(0, prefix.length) == prefix) isPrefix = true
            }
        }
        if (isPrefix) {
            if (text.length == ACCOUNT_LENGTH) "" else "Введите 20 цифр"
        } else {
            "Первые 5 цифр это 40702 либо 40802 либо 40817"
        }
    }

    private fun checkFocusAccountWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange) =
        Observable.just(with(focusChange) { predicateMessageChecker(accountPredicate, text, type) })

    private fun checkTextAccountWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange) =
        Observable.just(with(focusChange) { predicateMessageChecker(accountPredicate, text, type) })

    private val bikPredicate = { _: String ->
        if (bankEntity == null) {
            "Введите 9 цифр"
        } else {
            ""
        }
    }

    private fun checkFocusBikWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange) =
        Observable.just(with(focusChange) { predicateMessageChecker(bikPredicate, text, type) })

    private fun checkTextBikWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): Observable<CourierBillingAccountDataUIState> {
        return Observable.just(focusChange.text)
            .filter { it.length == KPP_LENGTH }
            .flatMapMaybe {
                _bicProgressState.value = true
                bankEntity = null
                interactor.getBank(it)
            }
            .doOnNext { bankEntity = it }
            .map<CourierBillingAccountDataUIState> {
                _bankNameState.value = it.name
                _bicProgressState.value = false
                _keyboardState.value = false
                CourierBillingAccountDataUIState.Complete(it.name, focusChange.type)
            }
            .onErrorReturn {
                _bicProgressState.value = false
                _bankNameState.value = "Введите БИК"
                bankEntity = null
                CourierBillingAccountDataUIState.Error(
                    "БИК банка не найден", focusChange.type
                )
            }
            .defaultIfEmpty(defaultBank(focusChange.type))
    }

    private val defaultBank = { type: CourierBillingAccountDataQueryType ->
        _bankNameState.value = "Введите БИК банка"
        bankEntity = null
        CourierBillingAccountDataUIState.Error("Введите 9 цифр", type)
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierBillingAccountDataUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .switchMap { mapActionFormChanges(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapActionFormChanges(action: CourierBillingAccountDataUIAction) = when (action) {
        is CourierBillingAccountDataUIAction.FocusChange -> checkFieldFocusChange(action)
        is CourierBillingAccountDataUIAction.TextChange -> checkFieldTextChange(action)
        is CourierBillingAccountDataUIAction.SaveClick -> checkFieldAll(action)
    }

    private fun checkFieldFocusChange(action: CourierBillingAccountDataUIAction.FocusChange) =
        when (action.type) {
            CourierBillingAccountDataQueryType.ACCOUNT -> checkFocusAccountWrapper(action)
            CourierBillingAccountDataQueryType.BIK -> checkFocusBikWrapper(action)
        }

    private fun checkFieldTextChange(action: CourierBillingAccountDataUIAction.TextChange) =
        when (action.type) {
            CourierBillingAccountDataQueryType.ACCOUNT -> checkTextAccountWrapper(action)
            CourierBillingAccountDataQueryType.BIK -> checkTextBikWrapper(action)
        }

    private fun checkFieldAll(action: CourierBillingAccountDataUIAction.SaveClick): Observable<CourierBillingAccountDataUIState> {
        holdAndProgress()
        val iterator = action.userData.iterator()
        while (iterator.hasNext()) {
            val nextItem = iterator.next()
            with(nextItem) {
                val predicate = when (type) {
                    CourierBillingAccountDataQueryType.ACCOUNT -> accountPredicate
                    CourierBillingAccountDataQueryType.BIK -> bikPredicate
                }
                if (isCompleteChecker(predicate, text, type)) iterator.remove()
            }
        }
        return Observable.just(
            if (action.userData.isEmpty()) {
                // TODO: 20.08.2021 выполнить загрузку данных пользователя
                CourierBillingAccountDataUIState.Next
            } else {
                _holderState.value = false
                _loaderState.value = CourierBillingAccountDataUILoaderState.Enable
                CourierBillingAccountDataUIState.ErrorFocus("", action.userData.first().type)
            }
        )
    }

    private fun holdAndProgress() {
        _holderState.value = true
        _loaderState.value = CourierBillingAccountDataUILoaderState.Progress
    }

    fun onSaveClick(accountEntity: CourierBillingAccountEntity) {
        saveAccount(accountEntity)
    }

    private fun saveAccount(accountEntity: CourierBillingAccountEntity) {
        addSubscription(
            interactor.saveAccountRemote(accountEntity, parameters.account).subscribe(
                { saveAccountComplete() },
                { saveAccountError(it) })
        )
    }

    private fun saveAccountComplete() {
        progressComplete()
        navigateToAccountSelector()
    }

    private fun progressComplete() {
        _holderState.value = false
        _loaderState.value = CourierBillingAccountDataUILoaderState.Disable
    }

    private fun navigateToAccountSelector() {
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
        _holderState.value = false
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
        holdAndProgress()
        addSubscription(
            interactor.removeAccount(parameters.account)
                .subscribe(
                    { removeAccountComplete() },
                    { removeAccountError() }
                )
        )
    }

    private fun removeAccountComplete() {
        progressComplete()
        navigateToBack()
    }

    private fun navigateToBack() {
        _navigationEvent.value = CourierBillingAccountDataNavAction.NavigateToBack
    }

    private fun removeAccountError() {
    }

    fun onSaveChangeAccountClick(courierDocumentsEntity: CourierBillingAccountEntity) {
        holdAndProgress()
        saveAccount(courierDocumentsEntity)
    }

}