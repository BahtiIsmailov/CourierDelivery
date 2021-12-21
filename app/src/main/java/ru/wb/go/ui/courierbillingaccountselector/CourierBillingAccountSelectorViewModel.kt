package ru.wb.go.ui.courierbillingaccountselector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbillingaccountselector.domain.CourierBillingAccountSelectorInteractor
import ru.wb.go.ui.dialogs.DialogStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import java.text.DecimalFormat
import java.util.*

class CourierBillingAccountSelectorViewModel(
    private val parameters: CourierBillingAccountSelectorAmountParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierBillingAccountSelectorInteractor,
    private val resourceProvider: CourierBillingAccountSelectorResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _balanceState = MutableLiveData<String>()
    val balanceState: LiveData<String>
        get() = _balanceState

    private val _navigateToMessageState = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToMessageState: LiveData<NavigateToDialogInfo>
        get() = _navigateToMessageState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigationEvent =
        SingleLiveEvent<CourierBillingAccountSelectorNavAction>()
    val navigationEvent: LiveData<CourierBillingAccountSelectorNavAction>
        get() = _navigationEvent

    private val _formUIState = MutableLiveData<CourierBillingAccountSelectorUIState>()
    val formUIState: LiveData<CourierBillingAccountSelectorUIState>
        get() = _formUIState

    private val _dropAccountState = MutableLiveData<CourierBillingAccountSelectorDropAction>()
    val dropAccountState: LiveData<CourierBillingAccountSelectorDropAction>
        get() = _dropAccountState

    private val _loaderState = MutableLiveData<CourierBillingAccountSelectorUILoaderState>()
    val loaderState: LiveData<CourierBillingAccountSelectorUILoaderState>
        get() = _loaderState

    private val _balanceChangeState = MutableLiveData<CourierBillingAccountSelectorBalanceAction>()
    val balanceChangeState: LiveData<CourierBillingAccountSelectorBalanceAction>
        get() = _balanceChangeState

    private var localBalance: Int = 0
    private var copyCourierBillingAccountEntity = mutableListOf<CourierBillingAccountEntity>()
    private var copyCourierBillingAccountSelectorAdapterItems =
        mutableListOf<CourierBillingAccountSelectorAdapterItem>()

    fun init() {
        localBalance = parameters.balance
        initToolbarLabel()
        observeNetworkState()
        fetchVersionApp()
        initBalance()
        initAccounts()
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = resourceProvider.getTitle()
    }

    private fun initBalance() {
        val decimalFormat = DecimalFormat("#,###.##")
        val balance = decimalFormat.format(localBalance)
        _balanceState.value = resourceProvider.getBalance(balance)
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

    private fun initAccounts() {
        addSubscription(interactor.accounts()
            .map { sortedAccounts(it) }
            .doOnSuccess { copyCourierBillingAccountEntity = it.toMutableList() }
            .map {
                val list = mutableListOf<CourierBillingAccountSelectorAdapterItem>()

                it.forEach {
                    val text = resourceProvider.getFormatAccount(it.bank, it.correspondentAccount)
                    val bankName = it.bank
                    val shortText = if (bankName.length > MAX_BANK_NAME_LENGTH) {
                        resourceProvider.getShortFormatAccount(
                            MAX_BANK_NAME_LENGTH,
                            bankName,
                            it.correspondentAccount
                        )
                    } else {
                        resourceProvider.getFormatAccount(it.bank, it.correspondentAccount)
                    }

                    list.add(CourierBillingAccountSelectorAdapterItem.Edit(text, shortText))
                }
                list.add(CourierBillingAccountSelectorAdapterItem.Add("Добавить счет"))
                list
            }
            .doOnSuccess { copyCourierBillingAccountSelectorAdapterItems = it.toMutableList() }
            .subscribe({
                _dropAccountState.value = CourierBillingAccountSelectorDropAction.SetItems(it)
            }, {})
        )
    }

    private fun sortedAccounts(accounts: List<CourierBillingAccountEntity>) =
        accounts.toMutableList()
            .sortedWith(compareBy({ it.bank }, { it.correspondentAccount.takeLast(4) }))

    private fun checkFocusSurnameWrapper(focusChange: CourierBillingAccountSelectorUIAction.FocusChange): CourierBillingAccountSelectorUIState {
        return checkSurname(focusChange.text, focusChange.type)
    }

    private fun checkTextSurnameWrapper(focusChange: CourierBillingAccountSelectorUIAction.TextChange): CourierBillingAccountSelectorUIState {
        return checkSurname(focusChange.text, focusChange.type)
    }

    private fun checkSurname(
        text: String,
        type: CourierBillingAccountSelectorQueryType
    ): CourierBillingAccountSelectorUIState {
        return if (text.isEmpty()) {
            val balance = decimalFormat(localBalance)
            CourierBillingAccountSelectorUIState.Empty(
                "Введите сумму от 0.01 до $balance ₽", type
            )
        } else {
            val balanceFromText = amountFromString(text)
            val balance = decimalFormat(balanceFromText)
            if (balanceFromText == 0) {
                CourierBillingAccountSelectorUIState.Error(balance, "Сумма недоступна", type)
            } else if (localBalance >= balanceFromText) {
                CourierBillingAccountSelectorUIState.Complete(balance, type)
            } else {
                CourierBillingAccountSelectorUIState.Error(balance, "Сумма недоступна", type)
            }
        }
    }

    private fun amountFromString(text: String) = text.replace("\\s".toRegex(), "").toInt()

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierBillingAccountSelectorUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
            .doOnNext { LogUtils { logDebugApp(it.toString()) } }
            .distinctUntilChanged()
            .map { mapAction(it) }
            .subscribe(
                { _formUIState.value = it },
                { LogUtils { logDebugApp(it.toString()) } })
        )
    }

    private fun mapAction(action: CourierBillingAccountSelectorUIAction) = when (action) {
        is CourierBillingAccountSelectorUIAction.FocusChange -> checkFieldFocus(action)
        is CourierBillingAccountSelectorUIAction.TextChange -> checkFieldText(action)
        is CourierBillingAccountSelectorUIAction.CompleteClick -> checkFieldAll(action)
    }

    private fun checkFieldAll(action: CourierBillingAccountSelectorUIAction.CompleteClick): CourierBillingAccountSelectorUIState {
        val iterator = action.userData.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            when (item.type) {
                CourierBillingAccountSelectorQueryType.SURNAME -> if (isNotCheck(
                        checkSurname(
                            item.text,
                            item.type
                        )
                    )
                ) iterator.remove()
            }
        }
        return if (action.userData.isEmpty()) {
            // TODO: 20.08.2021 выполнить загрузку данных пользователя
            CourierBillingAccountSelectorUIState.NextComplete
        } else {
            CourierBillingAccountSelectorUIState.ErrorFocus("", action.userData.first().type)
        }
    }

    private fun checkFieldFocus(action: CourierBillingAccountSelectorUIAction.FocusChange) =
        when (action.type) {
            CourierBillingAccountSelectorQueryType.SURNAME -> checkFocusSurnameWrapper(action)
        }

    private fun checkFieldText(action: CourierBillingAccountSelectorUIAction.TextChange) =
        when (action.type) {
            CourierBillingAccountSelectorQueryType.SURNAME -> {
                _balanceChangeState.value = if (action.text.isEmpty()) {
                    CourierBillingAccountSelectorBalanceAction.Init(resourceProvider.getWithdrawBalanceInit())
                } else {
                    val balanceFromText = amountFromString(action.text)
                    val balance = decimalFormat(balanceFromText)
                    val drawBalance = resourceProvider.getWithdrawBalance(balance)
                    if (balanceFromText == 0)
                        CourierBillingAccountSelectorBalanceAction.Error(drawBalance)
                    else if (localBalance >= balanceFromText)
                        CourierBillingAccountSelectorBalanceAction.Complete(drawBalance)
                    else
                        CourierBillingAccountSelectorBalanceAction.Error(drawBalance)
                }

                checkTextSurnameWrapper(action)
            }
        }

    private fun decimalFormat(balanceFromText: Int): String {
        val decimalFormat = DecimalFormat("#,###.##")
        return decimalFormat.format(balanceFromText)
    }

    private fun isNotCheck(state: CourierBillingAccountSelectorUIState) =
        state is CourierBillingAccountSelectorUIState.Complete

    fun onNextCompleteClick(accountId: Long, amount: String) {
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Progress
        val amountFromText = amountFromString(amount)
        val courierBillingAccountEntity = copyCourierBillingAccountEntity[accountId.toInt()]
        val paymentEntity = with(courierBillingAccountEntity) {
            PaymentEntity(
                amount = amountFromText,
                recipientBankName = bank,
                recipientName = userName,
                recipientBankBik = bic,
                recipientCorrespondentAccount = correspondentAccount,
                recipientAccount = "",
                recipientInn = inn,
                recipientKpp = ""
            )
        }
        addSubscription(
            interactor.payments(paymentEntity).subscribe(
                { paymentsComplete(amountFromText) },
                { paymentsError(it) })
        )
    }

    fun onEditAccountClick(idView: Int) {
        val account = copyCourierBillingAccountEntity[idView].correspondentAccount
        _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountEdit(
            account,
            localBalance
        )
    }

    fun onAddAccountClick() {
        _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountCreate(
            "", localBalance
        )
    }

    fun onAccountSelectClick(id: Int) {
        val selected = if (id == copyCourierBillingAccountEntity.size) {
            onAddAccountClick()
            0
        } else {
            id
        }
        _dropAccountState.value = CourierBillingAccountSelectorDropAction.SetSelected(selected)
    }

    private fun paymentsComplete(amount: Int) {
        localBalance -= amount
        initBalance()
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Disable
    }

    private fun paymentsError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.error.message,
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Enable
        _navigateToMessageState.value = message

    }

    override fun getScreenTag(): String {
        return ""
    }

    companion object {
        const val MAX_BANK_NAME_LENGTH = 23
    }

}