package ru.wb.go.ui.courierbilling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierBillingInteractor,
    private val dataBuilder: CourierBillingDataBuilder,
    private val resourceProvider: CourierBillingResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private var balance = 0

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
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

    init {
        observeNetworkState()
        fetchVersionApp()
        initToolbarLabel()
        initBalanceAndTransactions()
        initProgress()
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
        addSubscription(
            interactor.billing().subscribe(
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

    private fun billingError(throwable: Throwable) {
        onTechErrorLog("billingError", throwable)
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.WARNING.ordinal,
                resourceProvider.getGenericInternetTitleError(),
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.error.message,
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigateToDialogInfo.value = message
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
        onTechEventLog("onUpdateClick")
        showProgress()
        initBalanceAndTransactions()
    }

    fun onAccountClick() {
        showProgress()
        addSubscription(
            interactor.updateAccountsIsExist().subscribe(
                { accountsComplete(it) },
                { accountsError(it) })
        )
    }

    private fun accountsComplete(isAccountsExist: Boolean) {
        _navigationState.value = if (isAccountsExist)
            CourierBillingNavigationState.NavigateToAccountSelector(balance)
        else CourierBillingNavigationState.NavigateToAccountCreate("", balance)
        progressComplete()
    }

    private fun accountsError(throwable: Throwable) {
//        _navigationState.value = CourierBillingNavigationState.NavigateToDialogInfo(
//            DialogInfoStyle.ERROR.ordinal,
//            resourceProvider.getGenericServiceTitleError(),
//            throwable.toString(),
//            resourceProvider.getGenericServiceButtonError()
//        )
        _navigateToDialogInfo.value = messageError(throwable, resourceProvider)
        progressComplete()
    }

    fun onItemClick(id: Int) {

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