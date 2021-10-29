package ru.wb.perevozka.ui.courierbillingaccountselector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierbillingaccountselector.domain.CourierBillingAccountSelectorInteractor
import ru.wb.perevozka.ui.courierdata.Message
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.utils.LogUtils
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

    init {
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
        val balance = decimalFormat.format(parameters.balance)
        _balanceState.value = resourceProvider.getBalance(balance)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private var copyCourierBillingAccountEntity = mutableListOf<CourierBillingAccountEntity>()

    private fun initAccounts() {
        addSubscription(interactor.accounts()
            .doOnSuccess { copyCourierBillingAccountEntity = it.toMutableList() }
            .flatMap {
                Observable.fromIterable(it).map { it.bank + " " + "****" + it.account.takeLast(4) }
                    .toList()
            }
            .map {
                it.add("Добавить счет")
                it
            }
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
            CourierBillingAccountSelectorUIState.Error(text, type)
        } else {
            CourierBillingAccountSelectorUIState.Complete(text, type)
        }
    }

    fun onFormChanges(changeObservables: ArrayList<Observable<CourierBillingAccountSelectorUIAction>>) {
        addSubscription(Observable.merge(changeObservables)
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
            CourierBillingAccountSelectorUIState.Next
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
            CourierBillingAccountSelectorQueryType.SURNAME -> checkTextSurnameWrapper(action)
        }

    private fun isNotCheck(state: CourierBillingAccountSelectorUIState) =
        state is CourierBillingAccountSelectorUIState.Complete

    fun onNextClick(courierDocumentsEntity: CourierBillingAccountEntity) {
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Progress
        addSubscription(
            interactor.courierDocuments(courierDocumentsEntity).subscribe(
                { couriersFormComplete() },
                { couriersFormError(it) })
        )
    }

    fun onEditAccountClick(idView: Int) {
        if (idView == copyCourierBillingAccountEntity.size) {
            _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountCreate(
                "",
                parameters.balance
            )
        } else {
            val account = copyCourierBillingAccountEntity[idView].account
            _navigationEvent.value = CourierBillingAccountSelectorNavAction.NavigateToAccountEdit(
                account,
                parameters.balance
            )
        }
    }

    fun onCheckedClick(isComplete: Boolean, isAgreement: Boolean, isPersonal: Boolean) {
        _loaderState.value = if (isComplete && isAgreement && isPersonal) {
            CourierBillingAccountSelectorUILoaderState.Enable
        } else {
            CourierBillingAccountSelectorUILoaderState.Disable
        }
    }

    fun onAccountSelectClick(id: Int) {
        _dropAccountState.value = CourierBillingAccountSelectorDropAction.SetSelected(id)
    }

    private fun couriersFormComplete() {
        _loaderState.value = CourierBillingAccountSelectorUILoaderState.Disable
    }

    private fun couriersFormError(throwable: Throwable) {
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