package ru.wb.perevozka.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courier.CourierWarehouseEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import java.util.*

class CourierWarehousesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierWarehouseInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _warehouses = MutableLiveData<CourierWarehousesUIState>()
    val warehouses: LiveData<CourierWarehousesUIState>
        get() = _warehouses

    private val _navigationState = SingleLiveEvent<CourierWarehousesNavigationState>()
    val navigationState: LiveData<CourierWarehousesNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierWarehousesProgressState>()
    val progressState: LiveData<CourierWarehousesProgressState>
        get() = _progressState

    private var copyReceptionWarehouses = mutableListOf<CourierWarehousesItem>()

    init {
        observeSearch()
    }

    private fun getWarehouse() {
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
        _warehouses.value =
            if (items.isEmpty()) CourierWarehousesUIState.Empty(resourceProvider.getEmptyList())
            else CourierWarehousesUIState.InitItems(items)
        hideProgress()
    }

    private fun courierWarehouseError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> CourierWarehousesNavigationState.NavigateToDialogInfo(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> CourierWarehousesNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> CourierWarehousesNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigationState.value = message
        if (copyReceptionWarehouses.isEmpty()) {
            _warehouses.value = CourierWarehousesUIState.Empty(message.title)
        }
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierWarehousesProgressState.ProgressComplete
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
        _warehouses.value = CourierWarehousesUIState.Empty(resourceProvider.getSearchEmpty())
    }

    private fun convertWarehouses(warehouses: List<CourierWarehouseEntity>) =
        Observable.fromIterable(warehouses)
            .map { CourierWarehousesItem(it.id, it.name, it.fullAddress) }
            .toList()

    private fun saveConvertWarehouses(warehouses: List<CourierWarehousesItem>) {
        copyReceptionWarehouses = warehouses.toMutableList()
    }

    fun onUpdateClick() {
        showProgress()
        getWarehouse()
    }

    fun update() {
        getWarehouse()
    }

    fun onItemClick(index: Int) {
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
                            _navigationState.value =
                                CourierWarehousesNavigationState.NavigateToDialogInfo(
                                    DialogStyle.WARNING.ordinal,
                                    resourceProvider.getDialogEmptyTitle(),
                                    resourceProvider.getDialogEmptyMessage(),
                                    resourceProvider.getDialogEmptyButton(),
                                )
                        } else {
                            _navigationState.value =
                                CourierWarehousesNavigationState.NavigateToCourierOrder(
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

}