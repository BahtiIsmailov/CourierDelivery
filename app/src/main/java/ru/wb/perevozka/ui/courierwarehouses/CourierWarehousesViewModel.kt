package ru.wb.perevozka.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courier.CourierWarehouseEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractor
import java.util.*

class CourierWarehousesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierWarehouseInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _warehouse = MutableLiveData<CourierWarehousesUIState>()
    val warehouse: LiveData<CourierWarehousesUIState>
        get() = _warehouse

    private val _navigateUIState = SingleLiveEvent<CourierWarehousesUINavState>()
    val navigateUIState: LiveData<CourierWarehousesUINavState>
        get() = _navigateUIState

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessage: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _progressState = MutableLiveData<CourierWarehousesProgressState>()
    val progressState: LiveData<CourierWarehousesProgressState>
        get() = _progressState

    private var copyReceptionWarehouses = mutableListOf<CourierWarehousesItem>()

    init {
        observeSearch()
    }


    private fun getWarehouse() {
        showProgress()
        addSubscription(
            interactor.warehouses()
                .flatMap { convertWarehouses(it) }
                .doOnSuccess { saveConvertWarehouses(it) }
                .subscribe(
                    { courierWarehouseComplete(it) },
                    { courierWarehouseError(it) })
        )
    }

    private fun courierWarehouseComplete(items: MutableList<CourierWarehousesItem>) {
        _warehouse.value = if (items.isEmpty()) CourierWarehousesUIState.Empty("Извините, все заказы в работе")
        else CourierWarehousesUIState.ReceptionBoxesItem(items)
        hideProgress()
    }

    private fun courierWarehouseError(throwable: Throwable) {
        val message = getErrorMessage(throwable)
        showMessageError(message)
        progressComplete()
        warehouseErrorMessage(message)
    }

    private fun warehouseErrorMessage(message: String) {
        _warehouse.value = CourierWarehousesUIState.Empty(message)
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getErrorWarehouseDialogMessage()
        }
    }

    private fun progressComplete() {
        _progressState.value = CourierWarehousesProgressState.ProgressComplete
    }

    private fun showMessageError(message: String) {
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getWarehouseDialogTitle(),
            message,
            resourceProvider.getBoxPositiveButton()
        )
    }

    private fun observeSearch() {
        addSubscription(interactor.observeSearch()
            .map { text -> text.lowercase(Locale.getDefault()).trim() }
            .map { filterWarehouses(it) }
            .map { it.toMutableList() }
            .subscribe(
                { courierWarehouseComplete(it) },
                { observeSearchError() })
        )
    }

    private fun filterWarehouses(text: String) = if (text.isEmpty()) copyReceptionWarehouses
    else copyReceptionWarehouses.filter { it.name.lowercase(Locale.getDefault()).contains(text) }

    private fun observeSearchError() {
        _warehouse.value = CourierWarehousesUIState.Empty("Ничего не найдено")
    }

    private fun convertWarehouses(warehouses: List<CourierWarehouseEntity>) =
        Observable.fromIterable(warehouses)
            .map { CourierWarehousesItem(it.id, it.name, it.fullAddress) }
            .toList()

    private fun saveConvertWarehouses(warehouses: List<CourierWarehousesItem>) {
        copyReceptionWarehouses = warehouses.toMutableList()
    }

    fun onUpdateClick() {
        getWarehouse()
    }

    fun update() {
        getWarehouse()
    }

    fun onItemClick(index: Int, checked: Boolean) {
        showProgress()
        val oldItem = copyReceptionWarehouses[index].copy()
        addSubscription(
            interactor.warehouses()
                .flatMap { convertWarehouses(it) }
                .doOnSuccess { saveConvertWarehouses(it) }
                .subscribe(
                    { warehouseItems ->
                        if (warehouseItems.find { it.id == oldItem.id } == null) {
                            courierWarehouseComplete(warehouseItems)
                            _navigateUIState.value =
                                CourierWarehousesUINavState.NavigateToMessageInfo(
                                    "Извините, на складе " + oldItem.name + " все заказы в работе",
                                    "Ok"
                                )
                        } else {
                            _navigateUIState.value =
                                CourierWarehousesUINavState.NavigateToCourierOrder(
                                    oldItem.id,
                                    oldItem.name
                                )
                        }
                        hideProgress()
                    },
                    { courierWarehouseError(it) })
        )
    }

    private fun hideProgress() {
        _progressState.value = CourierWarehousesProgressState.ProgressComplete
    }

    private fun showProgress() {
        _progressState.value = CourierWarehousesProgressState.Progress
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}