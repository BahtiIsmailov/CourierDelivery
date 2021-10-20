package ru.wb.perevozka.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.AppConsts.MAP_WAREHOUSE_LON_DISTANCE
import ru.wb.perevozka.app.AppConsts.MAP_WAREHOUSE_LAT_DISTANCE
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.couriermap.*
import ru.wb.perevozka.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.utils.LogUtils
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

    private var coordinatePoints = mutableListOf<CoordinatePoint>()

    private fun saveWarehouseEntities(warehouseEntities: List<CourierWarehouseLocalEntity>) {
        this.warehouseEntities = warehouseEntities.toMutableList()
    }

    private fun saveCoordinatePoints(mapMarkers: List<CoordinatePoint>) {
        this.coordinatePoints = mapMarkers.toMutableList()
    }

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

    init {
        addSubscription(
            interactor.observeMapAction().subscribe({
                when (it) {
                    is CourierMapAction.ItemClick -> {
                    }
                    CourierMapAction.PermissionComplete -> {
                        LogUtils { logDebugApp("CourierMapAction.PermissionComplete getWarehouse()") }
                        getWarehouse()
                    }
                    is CourierMapAction.AutomatedLocationUpdate -> {
                    }
                    is CourierMapAction.ForcedLocationUpdate -> initMapByLocation(it.point)
                }
            },
                {}
            ))
    }

    fun update() {
        LogUtils { logDebugApp("update() getWarehouse()") }
        getWarehouse()
    }

    fun onUpdateClick() {
        showProgress()
        getWarehouse()
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

        //==========================================================================================
        // TODO: 04.10.2021 для тестирования
        mapMarkers.clear()
        val m = Empty(testMapPoint0(), resourceProvider.getWarehouseMapIcon())
        mapMarkers.add(m)
        val t = Empty(testMapPoint1(), resourceProvider.getWarehouseMapIcon())
        mapMarkers.add(t)

        coordinatePoints.clear()
        coordinatePoints.add(CoordinatePoint(testMapPoint0().lat, testMapPoint0().long))
        coordinatePoints.add(CoordinatePoint(testMapPoint1().lat, testMapPoint1().long))
        //==========================================================================================

        saveCoordinatePoints(coordinatePoints)
        saveMapMarkers(mapMarkers)
        interactor.mapState(CourierMapState.UpdateMyLocation)

    }

    private fun initMapByLocation(myLocation: CoordinatePoint) {
        LogUtils { logDebugApp("initMapByLocation(myLocation: CoordinatePoint) myLocation " + myLocation.toString()) }

//        val searchLocation = CoordinatePoint(
//            myLocation.latitude + MAP_WAREHOUSE_LAT_DISTANCE,
//            myLocation.longitude + MAP_WAREHOUSE_LON_DISTANCE
//        )
        val boundingBox = MapEnclosingCircle().minimumBoundingBoxRelativelyMyLocation(
            coordinatePoints, myLocation, MAP_WAREHOUSE_LAT_DISTANCE, MAP_WAREHOUSE_LON_DISTANCE
        )
        LogUtils { logDebugApp("initMapByLocation(myLocation: CoordinatePoint) boundingBox " + boundingBox) }
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateAndNavigateToMyLocationPoint(myLocation))
        interactor.mapState(CourierMapState.ZoomToCenterBoundingBox(boundingBox))
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
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
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
            clearSubscription()
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