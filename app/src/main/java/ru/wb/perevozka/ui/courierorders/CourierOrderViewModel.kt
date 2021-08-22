package ru.wb.perevozka.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.perevozka.ui.courierwarehouses.CourierWarehousesViewModel

class CourierOrderViewModel(
    private val parameters: CourierOrderParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderInteractor,
    private val dataBuilder: CourierOrderDataBuilder,
    private val resourceProvider: CourierOrderResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    val stateUIList = MutableLiveData<CourierOrderUIListState>()
    val progressState = MutableLiveData<CourierOrderProgressState>()

    private val _navigateToMessageInfo =
        MutableLiveData<CourierWarehousesViewModel.NavigateToMessageInfo>()
    val navigateToMessage: LiveData<CourierWarehousesViewModel.NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    init {
        observeNetworkState()
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

    private fun initOrders() {
        addSubscription(interactor.orders(parameters.currentWarehouseId)
            .flatMap { orders ->
                Observable.fromIterable(orders)
                    .map { dataBuilder.buildOrderItem(it) }
                    .toList()
            }
            .map {
                if (it.isEmpty()) CourierOrderUIListState.Empty("Извините, все заказы в работе")
                else CourierOrderUIListState.ShowOrders(it)
            }
            .subscribe({ ordersComplete(it) }, { ordersError(it) })
        )
    }

    private fun showProgress() {
        progressState.value = CourierOrderProgressState.Progress
    }

    private fun ordersComplete(courierOrderUIListState: CourierOrderUIListState) {
        progressState.value = CourierOrderProgressState.Complete
        stateUIList.value = courierOrderUIListState
    }

    private fun ordersError(throwable: Throwable) {
        val message = getErrorMessage(throwable)
        showMessageError(message)
        progressComplete()
    }

    private fun progressComplete() {
        progressState.value = CourierOrderProgressState.Complete
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getErrorOrderDialogMessage()
        }
    }

    private fun showMessageError(message: String) {
        _navigateToMessageInfo.value = CourierWarehousesViewModel.NavigateToMessageInfo(
            resourceProvider.getErrorOrderDialogTitle(),
            message,
            resourceProvider.getErrorOrderDialogPositiveButton()
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