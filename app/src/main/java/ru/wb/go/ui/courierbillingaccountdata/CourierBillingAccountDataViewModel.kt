package ru.wb.go.ui.courierbillingaccountdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.token.TokenManager
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
    tokenManager: TokenManager
) : NetworkViewModel(compositeDisposable) {

    companion object {
        private const val INN_LENGTH = 12
        private const val ACCOUNT_LENGTH = 20

        //        private const val BIK_LENGTH = 9
        private const val KPP_LENGTH = 9

        //        private const val COR_ACCOUNT_LENGTH = 20
//        private const val INN_BANK_LENGTH = 10
        private val PREFIX_ACCOUNT = listOf("40702", "40802", "40817")

    }

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _userDataState = MutableLiveData<UserData>()
    val userDataState: LiveData<UserData>
        get() = _userDataState

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

    private val _bankNameState = MutableLiveData<String>()
    val bankNameState: LiveData<String>
        get() = _bankNameState

    private val _loaderState = MutableLiveData<CourierBillingAccountDataUILoaderState>()
    val loaderState: LiveData<CourierBillingAccountDataUILoaderState>
        get() = _loaderState

    init {
        initToolbarLabel()
        observeNetworkState()
//        initStateField()
        initUserData(tokenManager)
    }

    private fun initUserData(tokenManager: TokenManager) {
        _userDataState.value = UserData(
            tokenManager.userName(),
            parameters.inn //tokenManager.userInn()
        )
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value =
            if (parameters.account.isEmpty()) resourceProvider.getTitleCreate() else resourceProvider.getTitleEdit()
    }

//    private fun initStateField() {
//        if (parameters.account.isEmpty()) _initUIState.value =
//            CourierBillingAccountDataInitUIState.Create else {
//            addSubscription(
//                interactor.getAccount(parameters.account)
//                    .subscribe(
//                        { _initUIState.value = CourierBillingAccountDataInitUIState.Edit(it) },
//                        { LogUtils { logDebugApp(it.toString()) } }) //_initUIState.value =CourierBillingAccountDataInitUIState.Create
//            )
//        }
//    }

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

//    private val surnamePredicate = { text: String -> text.isEmpty() }
//
//    private fun checkFocusSurnameWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange) =
//        Observable.just(with(focusChange) { predicateChecker(surnamePredicate, text, type) })
//
//    private fun checkTextSurnameWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange) =
//        Observable.just(with(focusChange) { predicateChecker(surnamePredicate, text, type) })


//    private val innPredicate = { text: String -> text.length != INN_LENGTH }
//
//    private fun checkFocusInnWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange) =
//        Observable.just(with(focusChange) { predicateChecker(innPredicate, text, type) })
//
//    private fun checkTextInnWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange) =
//        Observable.just(with(focusChange) { predicateChecker(innPredicate, text, type) })

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
            "первые 5 цифр это 40702 либо 40802 либо 40817"
        }
    }

    private fun checkFocusAccountWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange) =
        Observable.just(with(focusChange) { predicateMessageChecker(accountPredicate, text, type) })

    private fun checkTextAccountWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange) =
        Observable.just(with(focusChange) { predicateMessageChecker(accountPredicate, text, type) })


    private lateinit var bankEntity: BankEntity

    private val bikPredicate = { text: String -> text.length != KPP_LENGTH }

    private val bikForBankPredicate = { text: String ->
        _bicProgressState.value = true
        addSubscription(interactor.getBank(text).subscribe({
            bankEntity = it

        }, {


        }))
        text.length != KPP_LENGTH

    }

    private fun checkFocusBikWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange) =
        Observable.just(with(focusChange) { predicateChecker(bikPredicate, text, type) })

//    CourierBillingAccountDataUIState {
//        val message = predicate(text)
//        return if (message.isEmpty())
//            CourierBillingAccountDataUIState.Complete(text, type)
//        else
//    }

    private fun checkTextBikWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): Observable<CourierBillingAccountDataUIState> {
        _bicProgressState.value = true
        return Observable.just(focusChange.text)
            .filter { it.length == KPP_LENGTH }
            .flatMapMaybe { interactor.getBank(it) }
            .doOnNext { bankEntity = it }
            .doOnError { _bicProgressState.value = false }
            .map<CourierBillingAccountDataUIState> {
                _bankNameState.value = it.name
                CourierBillingAccountDataUIState.Complete(it.name, focusChange.type)
            }
            .onErrorReturn {
                _bankNameState.value = "Бик банка не найден"
                CourierBillingAccountDataUIState.Error(
                    it.toString(), focusChange.type
                )
            }
            .defaultIfEmpty(defaultBank(focusChange.type))
    }


//        return with(focusChange) { predicateChecker(bikPredicate, text, type) }
//    }

//    private val bankPredicate = { text: String -> text.isEmpty() }
//
//    private fun checkFocusBankWrapper(focusChange: CourierBillingAccountDataUIAction.FocusChange): CourierBillingAccountDataUIState {
//        return with(focusChange) { predicateChecker(bankPredicate, text, type) }
//    }
//
//    private fun checkTextBankWrapper(focusChange: CourierBillingAccountDataUIAction.TextChange): CourierBillingAccountDataUIState {
//        return with(focusChange) { predicateChecker(bankPredicate, text, type) }
//    }

    private val defaultBank = { type: CourierBillingAccountDataQueryType ->
        _bankNameState.value = "Введите БИК банка"
        CourierBillingAccountDataUIState.Complete("", type)
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierBillingAccountDataUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .switchMap { mapAction(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapAction(action: CourierBillingAccountDataUIAction) = when (action) {
        is CourierBillingAccountDataUIAction.FocusChange -> checkFieldFocusChange(action)
        is CourierBillingAccountDataUIAction.TextChange -> checkFieldTextChange(action)
        is CourierBillingAccountDataUIAction.CompleteClick -> checkFieldAll(action)
    }

    private fun checkFieldFocusChange(action: CourierBillingAccountDataUIAction.FocusChange) =
        when (action.type) {
//            CourierBillingAccountDataQueryType.SURNAME -> checkFocusSurnameWrapper(action)
//            CourierDataQueryType.INN -> checkFocusInnWrapper(action)
            CourierBillingAccountDataQueryType.ACCOUNT -> checkFocusAccountWrapper(action)
            CourierBillingAccountDataQueryType.BIK -> checkFocusBikWrapper(action)
//            CourierBillingAccountDataQueryType.BANK -> checkFocusBankWrapper(action)
        }

    private fun checkFieldTextChange(action: CourierBillingAccountDataUIAction.TextChange) =
        when (action.type) {
//            CourierBillingAccountDataQueryType.SURNAME -> checkTextSurnameWrapper(action)
//            CourierDataQueryType.INN -> checkTextInnWrapper(action)
            CourierBillingAccountDataQueryType.ACCOUNT -> checkTextAccountWrapper(action)
            CourierBillingAccountDataQueryType.BIK -> checkTextBikWrapper(action)
//            CourierBillingAccountDataQueryType.BANK -> checkTextBankWrapper(action)
        }

    private fun checkFieldAll(action: CourierBillingAccountDataUIAction.CompleteClick): Observable<CourierBillingAccountDataUIState> {
        val iterator = action.userData.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            with(item) {
                val predicate = when (type) {
//                    CourierBillingAccountDataQueryType.SURNAME -> surnamePredicate
//                    CourierDataQueryType.INN -> innPredicate
                    CourierBillingAccountDataQueryType.ACCOUNT -> accountPredicate
                    CourierBillingAccountDataQueryType.BIK -> bikPredicate
//                    CourierBillingAccountDataQueryType.BANK -> bankPredicate
                }
                //if (isCompleteChecker(predicate, text, type)) iterator.remove()
            }
        }
        return Observable.just(
            if (action.userData.isEmpty()) {
                // TODO: 20.08.2021 выполнить загрузку данных пользователя
                CourierBillingAccountDataUIState.Next
            } else {
                CourierBillingAccountDataUIState.ErrorFocus("", action.userData.first().type)
            }
        )


    }

    fun onSaveClick(accountEntity: CourierBillingAccountEntity) {
        saveAccount(accountEntity)
    }

    private fun saveAccount(accountEntity: CourierBillingAccountEntity) {
        _loaderState.value = CourierBillingAccountDataUILoaderState.Progress
        addSubscription(
            interactor.saveAccountRemote(accountEntity).subscribe(
                { saveAccountComplete() },
                { saveAccountError(it) })
        )
    }

    private fun saveAccountComplete() {
        _loaderState.value = CourierBillingAccountDataUILoaderState.Disable
        _navigationEvent.value =
            CourierBillingAccountDataNavAction.NavigateToAccountSelector(
                parameters.inn,
                parameters.amount
            )
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

    data class UserData(val userName: String, val userInn: String)

}