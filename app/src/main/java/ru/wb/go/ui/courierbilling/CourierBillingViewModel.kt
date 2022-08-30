package ru.wb.go.ui.courierbilling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierbilling.domain.CourierBillingInteractor
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CourierBillingViewModel(
    private val interactor: CourierBillingInteractor,
    private val dataBuilder: CourierBillingDataBuilder,
    private val resourceProvider: CourierBillingResourceProvider,
    private val userManager: UserManager,
    private val errorDialogManager: ErrorDialogManager
) : ServicesViewModel(interactor, resourceProvider) {

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
        _billingItems.value = CourierBillingState.Init
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = resourceProvider.getTitle()
    }

    private fun initBalanceAndTransactions() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch  {
            try {
                billingComplete(interactor.getBillingInfo())
            }catch (e:Exception){
                logException(e,"initBalanceAndTransactions")
                setLoader(WaitLoader.Complete)
                _billingItems.value = CourierBillingState.Empty("Ошибка получения данных")
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }
    }

    private fun billingComplete(it: BillingCommonEntity) {
        balance = it.balance

        _balanceInfo.value =  resourceProvider.formatMoney(balance, false)
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
        setLoader(WaitLoader.Complete)
    }

    fun canCheckout() = balance > 0

    private fun setLoader(state: WaitLoader) {
        _waitLoader.value = state
    }

    fun onUpdateClick() {
        //onTechEventLog("onUpdateClick")
        initBalanceAndTransactions()
    }

    fun gotoBillingAccountsClick() {
        _navigationState.value = CourierBillingNavigationState.NavigateToAccountSelector(balance)
    }

    fun onCancelLoadClick() {
        //viewModelScope.coroutineContext.cancelChildren()
        //clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierBilling"
    }



}