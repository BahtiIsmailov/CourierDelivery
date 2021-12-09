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
import ru.wb.go.ui.courierdata.Message
import ru.wb.go.ui.dialogs.DialogStyle
import ru.wb.go.utils.LogUtils
import java.text.DecimalFormat
import java.util.*

class CourierBillingAccountSelectorViewModel(
    private val parameters: CourierBillingAccountSelectorAmountParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierBillingAccountSelectorInteractor,
    private val resourceProvider: CourierBillingAccountSelectorResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _balanceState = MutableLiveData<String>()
    val balanceState: LiveData<String>
        get() = _balanceState

    private val _navigateToMessageState = SingleLiveEvent<Message>()
    val navigateToMessageState: LiveData<Message>
        get() = _navigateToMessageState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

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

    init {
        localBalance = parameters.balance
        initToolbarLabel()
        initBalance()
        initAccounts()
        observeNetworkState()
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

    private fun initAccounts() {
        addSubscription(interactor.accounts()
            .doOnSuccess { copyCourierBillingAccountEntity = it.toMutableList() }
            .map {
                val list = mutableListOf<CourierBillingAccountSelectorAdapterItem>()
                it.forEach {
                    list.add(
                        CourierBillingAccountSelectorAdapterItem.Edit(
                            resourceProvider.getFormatAccount(it.bank, it.account)
                        )
                    )
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
            CourierBillingAccountSelectorUIState.Empty("Введите сумму", type)
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
                    if (balanceFromText == 0) {
                        CourierBillingAccountSelectorBalanceAction.Error(
                            resourceProvider.getWithdrawBalance(balance)
                        )
                    } else if (localBalance >= balanceFromText) {
                        CourierBillingAccountSelectorBalanceAction.Complete(
                            resourceProvider.getWithdrawBalance(balance)
                        )
                    } else {
                        CourierBillingAccountSelectorBalanceAction.Error(
                            resourceProvider.getWithdrawBalance(balance)
                        )
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
        val courierBillingAccountEntity = copyCourierBillingAccountEntity[accountId.toInt()]
        val paymentEntity = with(courierBillingAccountEntity) {
            PaymentEntity(
                amount = amountFromText,
                recipientBankName = bank,
                recipientName = "$firstName $surName $middleName",
                recipientBankBik = bik,
                recipientCorrespondentAccount = corAccount,
                recipientAccount = account,
                recipientInn = innBank,
                recipientKpp = kpp
            )
        }
        addSubscription(
            interactor.payments(paymentEntity).subscribe(
                { paymentsComplete(amountFromText) },
                { paymentsError(it) })
        )
    }

    fun onEditAccountClick(idView: Int) {
        if (idView == copyCourierBillingAccountEntity.size) {
            _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountCreate(
                "",
                localBalance
            )
        } else {
            val account = copyCourierBillingAccountEntity[idView].account
            _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountEdit(
                account,
                localBalance
            )
        }
    }

    fun onAccountSelectClick(id: Int) {
        val selected = if (id == copyCourierBillingAccountEntity.size) 0 else id
        _dropAccountState.value = CourierBillingAccountSelectorDropAction.SetSelected(selected)
    }

    private fun paymentsComplete(amount: Int) {
        localBalance -= amount
        initBalance()
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Disable
    }

    private fun paymentsError(throwable: Throwable) {
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
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Enable
        _navigateToMessageState.value = message

    }

}