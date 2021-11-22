package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.managers.DeviceManager

class CourierOrdersViewModel(
    private val parameters: CourierOrderParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderInteractor,
    private val dataBuilder: CourierOrdersDataBuilder,
    private val resourceProvider: CourierOrdersResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _orders = MutableLiveData<CourierOrdersState>()
    val orders: LiveData<CourierOrdersState>
        get() = _orders

    private val _progressState = MutableLiveData<CourierOrdersProgressState>()
    val progressState: LiveData<CourierOrdersProgressState>
        get() = _progressState

    private val _navigationState = SingleLiveEvent<CourierOrdersNavigationState>()
    val navigationState: LiveData<CourierOrdersNavigationState>
        get() = _navigationState

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

    private fun initOrders() {
        addSubscription(interactor.orders(parameters.currentWarehouseId)
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
                    _navigateToDialogInfo.value = NavigateToDialogInfo(
                        DialogInfoStyle.WARNING.ordinal,
                        resourceProvider.getDialogTitle(),
                        resourceProvider.getDialogMessage(),
                        resourceProvider.getDialogButton()
                    )
                    CourierOrdersState.Empty(resourceProvider.getDialogEmpty())
                } else CourierOrdersState.ShowOrders(it)

            }
            .subscribe({ ordersComplete(it) }, { ordersError(it) })
        )
    }

    private fun showProgress() {
        _progressState.value = CourierOrdersProgressState.Progress
    }

    private fun ordersComplete(courierOrderUIListState: CourierOrdersState) {
        _progressState.value = CourierOrdersProgressState.Complete
        _orders.value = courierOrderUIListState
    }

    private fun ordersError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.WARNING.ordinal,
                throwable.message,
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
        _orders.value = CourierOrdersState.Empty(message.title)
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierOrdersProgressState.Complete
    }

    fun onItemClick(idView: Int) {
        val courierOrderEntity = copyCourierOrdersEntity[idView]
        addSubscription(
            interactor.clearAndSaveSelectedOrder(courierOrderEntity)
                .subscribe({
                    _navigationState.value =
                        CourierOrdersNavigationState.NavigateToOrderDetails(
                            parameters.address,
                            copyCourierOrdersEntity[idView]
                        )
                }, {})
        )
    }

    fun onUpdateClick() {
        showProgress()
        initOrders()
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    data class Label(val label: String)

}