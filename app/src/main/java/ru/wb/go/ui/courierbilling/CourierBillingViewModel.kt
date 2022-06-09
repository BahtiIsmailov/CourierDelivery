package ru.wb.go.ui.courierbilling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractor
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CourierBillingViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierBillingInteractor,
    private val dataBuilder: CourierBillingDataBuilder,
    private val resourceProvider: CourierBillingResourceProvider,
    private val userManager: UserManager,
    private val errorDialogManager: ErrorDialogManager
) : ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

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

    private val _billingItems = MutableLiveData<CourierBillingState>()
    val billingItems: LiveData<CourierBillingState>
        get() = _billingItems

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _navigationState = SingleLiveEvent<CourierBillingNavigationState>()
    val navigationState: LiveData<CourierBillingNavigationState>
        get() = _navigationState

    fun init() {
        clearPaymentGuid()
        initToolbarLabel()
        initBalanceAndTransactions()
        initProgress()
    }

    private fun clearPaymentGuid() {
        userManager.clearPaymentGuid()
    }

    private fun initProgress() {
        _billingItems.postValue( CourierBillingState.Init)
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.postValue( resourceProvider.getTitle())
    }

    private fun initBalanceAndTransactions() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                billingComplete(interactor.getBillingInfo())
            }catch (e:Exception){
                onTechErrorLog("billingError", e)
                setLoader(WaitLoader.Complete)
                _billingItems.postValue( CourierBillingState.Empty("Ошибка получения данных"))
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }
    }

    private fun billingComplete(it: BillingCommonEntity) {
        balance = it.balance

        _balanceInfo.postValue( resourceProvider.formatMoney(balance, false))
        val items = mutableListOf<BaseItem>()
        it.transactions.sortedByDescending { it.createdAt }
            .forEachIndexed { index, billingTransactionEntity ->
                items.add(dataBuilder.buildOrderItem(index, billingTransactionEntity))
            }
        if (items.isEmpty()) {
            _billingItems.postValue(
                CourierBillingState.Empty(resourceProvider.getEmptyList()))
        } else {
            _billingItems.postValue( CourierBillingState.ShowBilling(items))
        }
        setLoader(WaitLoader.Complete)
    }

    fun canCheckout() = balance > 0

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    fun onUpdateClick() {
        onTechEventLog("onUpdateClick")
        initBalanceAndTransactions()
    }

    fun gotoBillingAccountsClick() {
        _navigationState.postValue( CourierBillingNavigationState.NavigateToAccountSelector(balance))
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