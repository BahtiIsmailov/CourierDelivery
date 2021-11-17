package ru.wb.go.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.AppConsts.MAP_WAREHOUSE_LAT_DISTANCE
import ru.wb.go.app.AppConsts.MAP_WAREHOUSE_LON_DISTANCE
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint

class CourierWarehousesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierWarehouseInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _warehouses = MutableLiveData<CourierWarehouseItemState>()
    val warehouses: LiveData<CourierWarehouseItemState>
        get() = _warehouses

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

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

    }

    private fun observeMapAction() {
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
                {
                    LogUtils { logDebugApp("interactor.observeMapAction().subscribe " + it) }
                }
            ))
    }

    fun update() {
        LogUtils { logDebugApp("update()") }
        observeMapAction()
        LogUtils { logDebugApp("update() observeMapAction()") }
        getWarehouse()
        LogUtils { logDebugApp("update() getWarehouse()") }
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
        LogUtils { logDebugApp("courierWarehouseComplete() " + warehouses.toString()) }
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
//        mapMarkers.clear()
//        val m = Empty(testMapPoint0(), resourceProvider.getWarehouseMapIcon())
//        mapMarkers.add(m)
//        val t = Empty(testMapPoint1(), resourceProvider.getWarehouseMapIcon())
//        mapMarkers.add(t)
//
//        coordinatePoints.clear()
//        coordinatePoints.add(CoordinatePoint(testMapPoint0().lat, testMapPoint0().long))
//        coordinatePoints.add(CoordinatePoint(testMapPoint1().lat, testMapPoint1().long))
        //==========================================================================================

        saveCoordinatePoints(coordinatePoints)
        saveMapMarkers(mapMarkers)
        interactor.mapState(CourierMapState.UpdateMyLocation)

    }

    private fun initMapByLocation(myLocation: CoordinatePoint) {
        LogUtils { logDebugApp("initMapByLocation(myLocation: CoordinatePoint) myLocation " + myLocation.toString()) }
        val boundingBox = MapEnclosingCircle().minimumBoundingBoxRelativelyMyLocation(
            coordinatePoints, myLocation, MAP_WAREHOUSE_LAT_DISTANCE, MAP_WAREHOUSE_LON_DISTANCE
        )
        LogUtils { logDebugApp("initMapByLocation(myLocation: CoordinatePoint) boundingBox " + boundingBox) }
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateAndNavigateToMyLocationPoint(myLocation))
        interactor.mapState(CourierMapState.ZoomToCenterBoundingBox(boundingBox))
        progressComplete()
    }

    private fun courierWarehouseError(throwable: Throwable) {
        LogUtils { logDebugApp("courierWarehouseError() " + throwable.toString()) }
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigateToDialogInfo.value = message
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
            _navigateToDialogInfo.value =
                NavigateToDialogInfo(
                    DialogInfoStyle.WARNING.ordinal,
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