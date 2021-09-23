package ru.wb.perevozka.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.couriermap.CourierMapMarker
import ru.wb.perevozka.ui.couriermap.CourierMapState
import ru.wb.perevozka.ui.couriermap.Empty
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.utils.map.CoordinatePoint
import ru.wb.perevozka.utils.map.MapEnclosingCircle
import ru.wb.perevozka.utils.map.MapPoint

class CourierWarehousesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierWarehouseInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _warehouses = MutableLiveData<CourierWarehouseItemState>()
    val warehouses: LiveData<CourierWarehouseItemState>
        get() = _warehouses

    private val _navigationState = SingleLiveEvent<CourierWarehousesNavigationState>()
    val navigationState: LiveData<CourierWarehousesNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierWarehousesProgressState>()
    val progressState: LiveData<CourierWarehousesProgressState>
        get() = _progressState

    private var warehouseEntities = mutableListOf<CourierWarehouseLocalEntity>()

    private var warehouseItems = mutableListOf<CourierWarehouseItem>()

    private var mapMarkers = mutableListOf<CourierMapMarker>()

    private fun saveWarehouseEntities(warehouseEntities: List<CourierWarehouseLocalEntity>) {
        this.warehouseEntities = warehouseEntities.toMutableList()
    }

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

    private fun getWarehouse() {
        addSubscription(
            interactor.warehouses()
                .doOnSuccess { saveWarehouseEntities(it) }
                .subscribe(
                    { courierWarehouseComplete(it) },
                    { courierWarehouseError(it) })
        )
    }

    private fun courierWarehouseComplete(warehouses: List<CourierWarehouseLocalEntity>) {
        val warehouseItems = mutableListOf<CourierWarehouseItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val mapMarkers = mutableListOf<CourierMapMarker>()
        warehouses.forEachIndexed { index, item ->
            val warehouseItem = CourierWarehouseItem(item.id, item.name, item.fullAddress, false)
            warehouseItems.add(warehouseItem)
            coordinatePoints.add(CoordinatePoint(item.latitude, item.longitude))
            val mapPoint = MapPoint(index.toString(), item.latitude, item.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getWarehouseMapIcon())
            mapMarkers.add(mapMarker)
        }
        saveWarehouseItems(warehouseItems)
        initItems(warehouseItems)
        saveMapMarkers(mapMarkers)
        interactor.mapState(CourierMapState.UpdateMapMarkers(mapMarkers))
        val startNavigation = MapEnclosingCircle().minimumEnclosingCircle(coordinatePoints)
        interactor.mapState(CourierMapState.ZoomAllMarkers(startNavigation))
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
        if (warehouseItems.isEmpty()) {
            _warehouses.value = CourierWarehouseItemState.Empty(message.title)
        }
        progressComplete()
    }

    private fun progressComplete() {
        _progressState.value = CourierWarehousesProgressState.ProgressComplete
    }

    private fun initItems(warehouseItems: MutableList<CourierWarehouseItem>) {
        _warehouses.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.InitItems(warehouseItems)
    }

    private fun saveWarehouseItems(warehouses: List<CourierWarehouseItem>) {
        warehouseItems = warehouses.toMutableList()
    }

    fun onUpdateClick() {
        showProgress()
        getWarehouse()
    }

    fun update() {
        getWarehouse()
    }

    fun onItemClick(index: Int) {
        changeItemSelected(index)
    }

    private fun changeItemSelected(selectIndex: Int) {
        warehouseItems.forEachIndexed { index, item ->
            warehouseItems[index].isSelected =
                if (selectIndex == index) !item.isSelected
                else false
        }
        _warehouses.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.UpdateItems(selectIndex, warehouseItems)

        navigateToPoint(selectIndex, warehouseItems[selectIndex].isSelected)
    }

    private fun navigateToPoint(selectIndex: Int, isSelected: Boolean) {
        mapMarkers.forEach { item ->
            item.icon = if (item.point.id == selectIndex.toString()) {
                if (isSelected) resourceProvider.getWarehouseMapSelectedIcon() else resourceProvider.getWarehouseMapIcon()
            } else {
                resourceProvider.getWarehouseMapIcon()
            }
        }
        mapMarkers.find { it.point.id == selectIndex.toString() }?.apply {
            mapMarkers.remove(this)
            mapMarkers.add(this)
        }
        interactor.mapState(CourierMapState.UpdateMapMarkers(mapMarkers))
        interactor.mapState(CourierMapState.NavigateToMarker(selectIndex.toString()))
    }

    private fun checkAndNavigate(
        warehouseEntities: List<CourierWarehouseLocalEntity>,
        oldEntity: CourierWarehouseLocalEntity
    ) {
        if (warehouseEntities.find { it.id == oldEntity.id } == null) {
            courierWarehouseComplete(warehouseEntities)
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
    }

    fun onDetailClick(index: Int) {
        showProgress()
        val oldEntity = warehouseEntities[index].copy()
        addSubscription(
            interactor.warehouses()
                .doOnSuccess { saveWarehouseEntities(it) }
                .subscribe(
                    { checkAndNavigate(it, oldEntity) },
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