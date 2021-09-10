package ru.wb.perevozka.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle

class CourierOrdersViewModel(
    private val parameters: CourierOrderParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderInteractor,
    private val dataBuilder: CourierOrdersDataBuilder,
    private val resourceProvider: CourierOrdersResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

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
        initToolbarLabel()
        initOrders()
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

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = Label(parameters.address)
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
                    _navigationState.value = CourierOrdersNavigationState.NavigateToDialogInfo(
                        DialogStyle.WARNING.ordinal,
                        "Заказ забрали",
                        "Этот заказ уже взят в работу",
                        "Понятно"
                    )
                    CourierOrdersState.Empty("Этот заказ уже взят в работу")
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
            is NoInternetException -> CourierOrdersNavigationState.NavigateToDialogInfo(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> CourierOrdersNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> CourierOrdersNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigationState.value = message
        _orders.value = CourierOrdersState.Empty(message.title)
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierOrdersProgressState.Complete
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