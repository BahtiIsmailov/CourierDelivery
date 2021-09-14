package ru.wb.perevozka.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle

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

    private var copyWarehousesEntity = mutableListOf<CourierWarehouseLocalEntity>()

    init {
        //observeSearch()
    }

    private fun getWarehouse() {
        addSubscription(
            interactor.warehouses()
                .doOnSuccess { saveWarehousesEntity(it) }
                .flatMap { convertWarehouses(it) }
                .subscribe(
                    { courierWarehousesComplete(it) },
                    { courierWarehouseError(it) })
        )
    }

    private fun courierWarehousesComplete(items: MutableList<CourierWarehousesItem>) {
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
        if (copyWarehousesEntity.isEmpty()) {
            _warehouses.value = CourierWarehousesUIState.Empty(message.title)
        }
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierWarehousesProgressState.ProgressComplete
    }

//    private fun observeSearch() {
//        addSubscription(interactor.observeSearch()
//            .map { text -> text.lowercase(Locale.getDefault()).trim() }
//            .map { filterWarehouses(it) }
//            .map { it.toMutableList() }
//            .subscribe(
//                { courierWarehouseComplete(it) },
//                { observeSearchError() })
//        )
//    }

//    private fun filterWarehouses(text: String) = if (text.isEmpty()) copyReceptionWarehouses
//    else copyReceptionWarehouses.filter { it.name.lowercase(Locale.getDefault()).contains(text) }
//
//    private fun observeSearchError() {
//        _warehouses.value = CourierWarehousesUIState.Empty(resourceProvider.getSearchEmpty())
//    }

    private fun convertWarehouses(warehouses: List<CourierWarehouseLocalEntity>) =
        Observable.fromIterable(warehouses)
            .map { CourierWarehousesItem(it.id, it.name, it.fullAddress) }
            .toList()

    private fun saveWarehousesEntity(warehouses: List<CourierWarehouseLocalEntity>) {
        copyWarehousesEntity = warehouses.toMutableList()
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
        val oldEntity = copyWarehousesEntity[index].copy()
        addSubscription(
            interactor.warehouses()
                .doOnSuccess { saveWarehousesEntity(it) }
                .flatMap { convertWarehouses(it) }
                .subscribe(
                    { warehouseItems ->
                        if (warehouseItems.find { it.id == oldEntity.id } == null) {
                            courierWarehousesComplete(warehouseItems)
                            _navigationState.value =
                                CourierWarehousesNavigationState.NavigateToDialogInfo(
                                    DialogStyle.WARNING.ordinal,
                                    resourceProvider.getDialogEmptyTitle(),
                                    resourceProvider.getDialogEmptyMessage(),
                                    resourceProvider.getDialogEmptyButton(),
                                )
                        } else {
                            interactor.clearAndSaveCurrentWarehouses(oldEntity).subscribe()
                            _navigationState.value =
                                CourierWarehousesNavigationState.NavigateToCourierOrder(
                                    oldEntity.id,
                                    oldEntity.name
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