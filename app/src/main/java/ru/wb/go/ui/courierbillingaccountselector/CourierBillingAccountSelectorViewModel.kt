package ru.wb.go.ui.courierbillingaccountselector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbillingaccountselector.domain.CourierBillingAccountSelectorInteractor
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import java.text.DecimalFormat

class CourierBillingAccountSelectorViewModel(
    private val parameters: CourierBillingAccountSelectorAmountParameters,
    private val interactor: CourierBillingAccountSelectorInteractor,
    private val resourceProvider: CourierBillingAccountSelectorResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
) : ServicesViewModel(interactor, resourceProvider) {

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _balanceState = MutableLiveData<String>()
    val balanceState: LiveData<String>
        get() = _balanceState

    private val _errorDialogState = SingleLiveEvent<ErrorDialogData>()
    val errorDialogState: LiveData<ErrorDialogData>
        get() = _errorDialogState

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

    private val _courierInnLivaData = MutableLiveData<String>()
    val courierInnLivaData: LiveData<String> = _courierInnLivaData

    private val _balanceChangeState = MutableLiveData<CourierBillingAccountSelectorBalanceAction>()
    val balanceChangeState: LiveData<CourierBillingAccountSelectorBalanceAction>
        get() = _balanceChangeState

    private var localBalance: Int = 0
    private var billingAccounts = mutableListOf<CourierBillingAccountEntity>()
    private var copyCourierBillingAccountSelectorAdapterItems =
        mutableListOf<CourierBillingAccountSelectorAdapterItem>()

    fun init() {
        localBalance = parameters.balance
        initToolbarLabel()
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

    private fun setLoader(state: CourierBillingAccountSelectorUILoaderState) {
        _loaderState.value = state
    }

    private fun initAccounts() {
        setLoader(CourierBillingAccountSelectorUILoaderState.Progress)
        viewModelScope.launch  {
            try {
                billingAccounts = interactor.getBillingAccounts().toMutableList()
                billingAccounts.map {
                    _courierInnLivaData.value = it.inn
                }
                copyCourierBillingAccountSelectorAdapterItems = convertToItems(billingAccounts)
                setLoader(CourierBillingAccountSelectorUILoaderState.Complete)
                _dropAccountState.value =
                    CourierBillingAccountSelectorDropAction.SetItems(
                        copyCourierBillingAccountSelectorAdapterItems
                    )

            } catch (e: Exception) {
                onTechErrorLog("getBillingAccounts", e)
                errorDialogManager.showErrorDialog(e, _errorDialogState)
            }
        }

    }

    private fun convertToItems(it: List<CourierBillingAccountEntity>): MutableList<CourierBillingAccountSelectorAdapterItem> {
        val list = mutableListOf<CourierBillingAccountSelectorAdapterItem>()
        it.forEach {
            list.add(
                CourierBillingAccountSelectorAdapterItem.Edit(
                    it.bank,
                    it.account.takeLast(4)
                )
            )
        }
        list.add(CourierBillingAccountSelectorAdapterItem.Add("Добавить счет"))
        return list
    }

    private fun sortedAccounts(accounts: List<CourierBillingAccountEntity>) =
        accounts.toMutableList()
            .sortedWith(compareBy({ it.bank }, { it.account.takeLast(4) }))

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
                "Введите сумму от 1 до $balance ₽", type
            )
        } else {
            val balanceFromText = amountFromString(text)
            val balance = decimalFormat(balanceFromText)
            when {
                balanceFromText == 0 -> {
                    CourierBillingAccountSelectorUIState.Error(balance, "Сумма недоступна", type)
                }
                localBalance >= balanceFromText -> {
                    CourierBillingAccountSelectorUIState.Complete(balance, type)
                }
                else -> {
                    CourierBillingAccountSelectorUIState.Error(balance, "Сумма недоступна", type)
                }
            }
        }
    }

    private fun amountFromString(text: String) = text.replace("\\s".toRegex(), "").toInt()

    fun onFormChanges(changeObservables: ArrayList<Flow<CourierBillingAccountSelectorUIAction>>) {
        changeObservables
            .merge()
            .distinctUntilChanged()
            .map { mapAction(it) }
            .onEach { _formUIState.value = it }
            .catch { LogUtils { logDebugApp(it.toString()) } }
            .launchIn(viewModelScope)
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
                    when {
                        balanceFromText == 0 -> CourierBillingAccountSelectorBalanceAction.Error(
                            drawBalance
                        )
                        localBalance >= balanceFromText -> CourierBillingAccountSelectorBalanceAction.Complete(
                            drawBalance
                        )
                        else -> CourierBillingAccountSelectorBalanceAction.Error(drawBalance)
                    }
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
        val courierBillingAccountEntity = billingAccounts[accountId.toInt()]
        val paymentEntity = with(courierBillingAccountEntity) {
            PaymentEntity(
                recipientBankName = bank,
                recipientName = userName,
                recipientBankBik = bic,
                recipientCorrespondentAccount = correspondentAccount,
                recipientAccount = account,
                recipientInn = inn
            )
        }
        viewModelScope.launch  {
            try {
                interactor.payments(amountFromText, paymentEntity)
                paymentsComplete(amountFromText)
            } catch (e: Exception) {
                onTechErrorLog("requestPayout", e)
                errorDialogManager.showErrorDialog(e, _errorDialogState)
            }
        }
    }

    fun onEditAccountClick(idView: Int) {
        _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountEdit(
            billingAccounts[idView],
            billingAccounts,
            parameters.balance
        )
    }

    fun onAddAccountClick() {
        _navigationEvent.value =
            CourierBillingAccountSelectorNavAction.NavigateToAccountCreate(
                billingAccounts,
                parameters.balance
            )
    }

    fun onAccountSelectClick(id: Int) {
        val selected = if (id == billingAccounts.size) {
            onAddAccountClick()
            0
        } else {
            id
        }
        _dropAccountState.value = CourierBillingAccountSelectorDropAction.SetSelected(selected)
    }

    private fun paymentsComplete(amount: Int) {
        localBalance -= amount
        parameters.balance = localBalance
        initBalance()
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Disable
        _navigationEvent.value =
            CourierBillingAccountSelectorNavAction.NavigateToBillingComplete(amount)
    }

    override fun getScreenTag(): String {
        return ""
    }


}