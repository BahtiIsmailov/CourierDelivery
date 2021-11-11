package ru.wb.go.ui.courierbilling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingTransactionEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractor
import ru.wb.go.ui.dialogs.DialogStyle
import java.text.DecimalFormat

class CourierBillingViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierBillingInteractor,
    private val dataBuilder: CourierBillingDataBuilder,
    private val resourceProvider: CourierBillingResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _balanceInfo = MutableLiveData<String>()
    val balanceInfo: LiveData<String>
        get() = _balanceInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _billingItems = MutableLiveData<CourierBillingState>()
    val billingItems: LiveData<CourierBillingState>
        get() = _billingItems

    private val _progressState = MutableLiveData<CourierBillingProgressState>()
    val progressState: LiveData<CourierBillingProgressState>
        get() = _progressState

    private val _navigationState = SingleLiveEvent<CourierBillingNavigationState>()
    val navigationState: LiveData<CourierBillingNavigationState>
        get() = _navigationState

    init {
        initToolbarLabel()
        initBalanceAndTransactions()
        initProgress()
    }

    private fun initProgress() {
        _billingItems.value = CourierBillingState.Init
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = Label(resourceProvider.getTitle())
    }

    private fun initBalanceAndTransactions() {
        addSubscription(
            interactor.billing().subscribe(
                {
                    val decimalFormat = DecimalFormat("#,###.##")
                    val balance = decimalFormat.format(it.balance)
                    _balanceInfo.value = resourceProvider.getAmount(balance)
                    val items = mutableListOf<BaseItem>()
                    it.transactions.forEachIndexed { index, billingTransactionEntity ->
                        items.add(dataBuilder.buildOrderItem(index, billingTransactionEntity))
                    }

//                    items.clear()
//                    _balanceInfo.value = resourceProvider.getAmount("21 400")
//                    for (i in 1..10) {
//                        val billing = BillingTransactionEntity(
//                            "5122hhskkjh9", if (i % 2 > 0) {
//                                1000 * i * -1
//                            } else {
//                                5000 * i
//                            },
//                            if (i % 2 > 0) {
//                                "2021-04-22T12:32:25+03:00"
//                            } else {
//                                "2021-04-25T16:32:25+03:00"
//                            }
//                        )
//                        items.add(dataBuilder.buildOrderItem(i, billing))
//                    }

                    if (items.isEmpty()) {
                        _billingItems.value =
                            CourierBillingState.Empty(resourceProvider.getEmptyList())
                    } else {
                        _billingItems.value = CourierBillingState.ShowBilling(items)
                    }
                    progressComplete()
                },

                {
                    billingError(it)
                })
        )
    }

    private fun billingError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> CourierBillingNavigationState.NavigateToDialogInfo(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> CourierBillingNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> CourierBillingNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigationState.value = message
        _billingItems.value = CourierBillingState.Empty(message.title)
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierBillingProgressState.Complete
    }

    private fun showProgress() {
        _progressState.value = CourierBillingProgressState.Progress
    }

    fun onUpdateClick() {
        showProgress()
        initBalanceAndTransactions()
    }

    fun onItemClick(id: Int) {

    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    data class Label(val label: String)

}