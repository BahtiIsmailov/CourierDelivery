package ru.wb.go.ui.courierbillingaccountdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbillingaccountdata.domain.CourierBillingAccountDataInteractor
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CourierBillingAccountDataViewModel(
    private val parameters: CourierBillingAccountDataAmountParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierBillingAccountDataInteractor,
    private val resourceProvider: CourierBillingAccountDataResourceProvider,
    private val tokenManager: TokenManager,
    private val deviceManager: DeviceManager,
    private val errorDialogManager: ErrorDialogManager
) : NetworkViewModel(compositeDisposable, metric) {

    companion object {
        private const val ACCOUNT_LENGTH = 20
        private const val KPP_LENGTH = 9
        private val PREFIX_ACCOUNT = listOf("40702", "40802", "40817")
    }

    private var bankEntity: BankEntity? = null

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _initUIState = MutableLiveData<CourierBillingAccountDataInitUIState>()
    val initUIState: LiveData<CourierBillingAccountDataInitUIState>
        get() = _initUIState

    private val _navigateToMessageState = SingleLiveEvent<ErrorDialogData>()
    val navigateToMessageState: LiveData<ErrorDialogData>
        get() = _navigateToMessageState

    private val _navigateToDialogConfirmInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmInfo

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

    private val _bankFindState = MutableLiveData<BankFind>()
    val bankFindState: LiveData<BankFind>
        get() = _bankFindState

    private val _loaderState = MutableLiveData<CourierBillingAccountDataUILoaderState>()
    val loaderState: LiveData<CourierBillingAccountDataUILoaderState>
        get() = _loaderState

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    init {
        initToolbarLabel()
        observeNetworkState()
        fetchVersionApp()
        initStateField()
    }

    fun getParams(): CourierBillingAccountDataAmountParameters {
        return parameters
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value =
            if (parameters.account == null) resourceProvider.getTitleCreate()
            else resourceProvider.getTitleEdit()
    }

    private fun initStateField() {
        if (parameters.account == null) createAccount()
        else {
            with(parameters.account) {
                bankEntity = BankEntity(bic, bank, correspondentAccount, false)
            }
        }
    }

    private fun createAccount() {
        _initUIState.value = CourierBillingAccountDataInitUIState.Create(
            tokenManager.userName(),
            tokenManager.userInn()
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
        // FIXME: Необходимо еще проверить БИК, чтобы утверждать, что такой счет заведен
        if (isPrefix) {
            if (text.length == ACCOUNT_LENGTH) {
                if (parameters.billingAccounts.find { it.account == text } != null) {
                    "Такой счет уже заведен"
                } else
                    ""
            } else "Введите 20 цифр"
        } else {
            "Счет должен начинаться с 40702 или 40802 или 40817"
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
        Observable.just(with(focusChange) {
            predicateMessageChecker(bikPredicate, text, type)
        })

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
                _bankFindState.value = BankFind(it.name, it.correspondentAccount)
                _bicProgressState.value = false
                _keyboardState.value = false
                CourierBillingAccountDataUIState.Complete(it.name, focusChange.type)
            }
            .onErrorReturn {
                _bicProgressState.value = false
                _bankFindState.value = BankFind("Введите БИК", "")
                bankEntity = null
                CourierBillingAccountDataUIState.Error(
                    "БИК банка не найден", focusChange.type
                )
            }
            .defaultIfEmpty(defaultBank(focusChange.type))
    }

    private val defaultBank = { type: CourierBillingAccountDataQueryType ->
        _bankFindState.value = BankFind("Введите БИК банка", "")
        bankEntity = null
        CourierBillingAccountDataUIState.Error("Введите 9 цифр", type)
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierBillingAccountDataUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .flatMap { mapActionFormChanges(it) }
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

    private fun checkFieldAll(action: CourierBillingAccountDataUIAction.SaveClick):
            Observable<CourierBillingAccountDataUIState> {
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
                CourierBillingAccountDataUIState.Next
            } else {
                CourierBillingAccountDataUIState.ErrorFocus("", action.userData.first().type)
            }
        )
    }

    private fun setButtonAndLoaderState(isAction: Boolean) {
        if (isAction) {
            _loaderState.value = CourierBillingAccountDataUILoaderState.Disable
            _waitLoader.postValue(WaitLoader.Wait)
        } else {
            _loaderState.value = CourierBillingAccountDataUILoaderState.Enable
            _waitLoader.postValue(WaitLoader.Complete)
        }
    }

    fun onSaveAccountClick(changedAccount: CourierBillingAccountEntity) {
        setButtonAndLoaderState(true)
        val newData = parameters.billingAccounts.toMutableList()
        if (parameters.account != null) {
            newData.remove(parameters.account)
        }
        newData.add(changedAccount)
        addSubscription(
            interactor.saveBillingAccounts(newData)
                .subscribe(
                    { saveAccountComplete() },
                    {
                        setButtonAndLoaderState(false)
                        errorDialogManager.showErrorDialog(it, _navigateToMessageState)
                    })
        )
    }

    private fun saveAccountComplete() {
        setButtonAndLoaderState(false)
        navigateToAccountSelector()
    }

    private fun navigateToAccountSelector() {
        _navigationEvent.value =
            CourierBillingAccountDataNavAction.NavigateToAccountSelector(parameters.balance)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    fun onRemoveAccountClick() {
        _navigationEvent.value =
            CourierBillingAccountDataNavAction.NavigateToConfirmDialog(parameters.account!!.account)

    }

    private fun removeAccountComplete() {
        setButtonAndLoaderState(false)
        _navigationEvent.value = CourierBillingAccountDataNavAction.NavigateToBack
    }

    fun getBankEntity() = bankEntity

    override fun getScreenTag(): String {
        return ""
    }

    fun removeConfirmed() {
        val newList = parameters.billingAccounts.filter {
            it != parameters.account
        }
        assert(newList.isNotEmpty())
        setButtonAndLoaderState(true)
        addSubscription(
            interactor.saveBillingAccounts(newList)
                .subscribe(
                    { removeAccountComplete() },
                    {
                    setButtonAndLoaderState(false)
                        errorDialogManager.showErrorDialog(it,_navigateToMessageState)
                    }
                )
        )
    }

    data class BankFind(val name: String, val corAccount: String)

}