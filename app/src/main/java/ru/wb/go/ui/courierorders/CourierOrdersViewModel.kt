package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CourierOrdersViewModel(
    private val parameters: CourierOrderParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrderInteractor,
    private val dataBuilder: CourierOrdersDataBuilder,
    private val resourceProvider: CourierOrdersResourceProvider,
    private val deviceManager: DeviceManager,
    private val errorDialogManager: ErrorDialogManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _orders = MutableLiveData<CourierOrdersState>()
    val orders: LiveData<CourierOrdersState>
        get() = _orders

    private val _navigationState = SingleLiveEvent<CourierOrdersNavigationState>()
    val navigationState: LiveData<CourierOrdersNavigationState>
        get() = _navigationState

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private var copyCourierOrdersEntity = mutableListOf<CourierOrderEntity>()

    init {
        observeNetworkState()
        fetchVersionApp()
        initToolbarLabel()
        initOrders()
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = Label(parameters.address)
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private fun initOrders() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.orders(parameters.currentWarehouseId)
                .doOnSuccess { copyCourierOrdersEntity = it.toMutableList() }
                .flatMap { orders ->
                    Observable.fromIterable(orders.withIndex())
                        .map { (index, item): IndexedValue<CourierOrderEntity> ->
                            dataBuilder.buildOrderItem(
                                index,
                                item
                            )
                        }
                        .toList()
                }
                .map {
                    if (it.isEmpty()) {
                        CourierOrdersState.Empty(resourceProvider.getDialogEmpty())
                    } else CourierOrdersState.ShowOrders(it)

                }
                .subscribe(
                    { ordersComplete(it) }, {
                        onTechErrorLog("ordersError", it)
                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                        _orders.value = CourierOrdersState.Empty("Ошибка получения данных")
                        setLoader(WaitLoader.Complete)
                    })
        )
    }

    private fun ordersComplete(courierOrderUIListState: CourierOrdersState) {
        _orders.value = courierOrderUIListState
        setLoader(WaitLoader.Complete)
    }

    fun onItemClick(idView: Int) {
        onTechEventLog("onItemClick")
        val courierOrderEntity = copyCourierOrdersEntity[idView]
        addSubscription(
            interactor.clearAndSaveSelectedOrder(courierOrderEntity)
                .subscribe(
                    { clearAndSaveSelectedOrderComplete(idView) },
                    {
                        onTechErrorLog("ordersError", it)
                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                    })
        )
    }

    private fun clearAndSaveSelectedOrderComplete(idView: Int) {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(
                parameters.address,
                copyCourierOrdersEntity[idView]
            )
    }

    fun onUpdateClick() {
        initOrders()
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrders"
    }

    data class Label(val label: String)

}