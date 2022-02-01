package ru.wb.go.ui.courierbilling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractor
import ru.wb.go.ui.courierbillingaccountselector.domain.CourierBillingAccountSelectorInteractor
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CourierBillingViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierBillingInteractor,
    private val dataBuilder: CourierBillingDataBuilder,
    private val resourceProvider: CourierBillingResourceProvider,
    private val deviceManager: DeviceManager,
    private val userManager: UserManager,
    private val interactorSelector: CourierBillingAccountSelectorInteractor,
    private val errorDialogManager: ErrorDialogManager
) : NetworkViewModel(compositeDisposable, metric) {

    private var balance = 0

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _balanceInfo = MutableLiveData<String>()
    val balanceInfo: LiveData<String>
        get() = _balanceInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _billingItems = MutableLiveData<CourierBillingState>()
    val billingItems: LiveData<CourierBillingState>
        get() = _billingItems

    private val _progressState = MutableLiveData<CourierBillingProgressState>()
    val progressState: LiveData<CourierBillingProgressState>
        get() = _progressState

    private val _navigationState = SingleLiveEvent<CourierBillingNavigationState>()
    val navigationState: LiveData<CourierBillingNavigationState>
        get() = _navigationState

    fun init() {
        clearPaymentGuid()
        observeNetworkState()
        fetchVersionApp()
        initToolbarLabel()
        initBalanceAndTransactions()
        initProgress()
    }

    private fun clearPaymentGuid() {
        userManager.clearPaymentGuid()
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

    private fun initProgress() {
        _billingItems.value = CourierBillingState.Init
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = resourceProvider.getTitle()
    }

    private fun initBalanceAndTransactions() {
        showProgress()
        addSubscription(
            interactor.getBillingInfo()
                .subscribe(
                    { billingComplete(it) },
                    { billingError(it) })
        )
    }

    private fun billingComplete(it: BillingCommonEntity) {
        balance = it.balance

        _balanceInfo.value = resourceProvider.formatMoney(balance, false)
        val items = mutableListOf<BaseItem>()
        it.transactions.sortedByDescending { it.createdAt }
            .forEachIndexed { index, billingTransactionEntity ->
                items.add(dataBuilder.buildOrderItem(index, billingTransactionEntity))
            }
        if (items.isEmpty()) {
            _billingItems.value =
                CourierBillingState.Empty(resourceProvider.getEmptyList())
        } else {
            _billingItems.value = CourierBillingState.ShowBilling(items)
        }
        progressComplete()
    }

    fun canCheckout() = balance > 0

    private fun billingError(throwable: Throwable) {
        onTechErrorLog("billingError", throwable)
        errorDialogManager.showErrorDialog(throwable, _navigateToDialogInfo)
        _billingItems.value = CourierBillingState.Empty("Ошибка получения данных")
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierBillingProgressState.Complete
    }

    private fun showProgress() {
        _progressState.value = CourierBillingProgressState.Progress
    }

    fun onUpdateClick() {
        onTechEventLog("onUpdateClick")
        initBalanceAndTransactions()
    }

    fun gotoBillingAccountsClick() {
        showProgress()
        addSubscription(
            interactorSelector
                .getBillingAccounts()
                .subscribe(
                    { accountsComplete(it) },
                    { accountsError(it) })
        )
    }

    private fun accountsComplete(accounts: List<CourierBillingAccountEntity>) {
        _navigationState.value = if (accounts.isNotEmpty())
            CourierBillingNavigationState.NavigateToAccountSelector(balance, accounts)
        else CourierBillingNavigationState.NavigateToAccountCreate(null, listOf(), balance)
        progressComplete()
    }

    private fun accountsError(throwable: Throwable) {
        progressComplete()
        errorDialogManager.showErrorDialog(throwable,_navigateToDialogInfo)
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierBilling"
    }

}