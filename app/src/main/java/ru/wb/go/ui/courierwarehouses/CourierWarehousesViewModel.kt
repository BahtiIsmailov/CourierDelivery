package ru.wb.go.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.MY_LOCATION_ID
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehousesInteractor
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint

class CourierWarehousesViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierWarehousesInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _warehouseState = MutableLiveData<CourierWarehouseItemState>()
    val warehouseState: LiveData<CourierWarehouseItemState>
        get() = _warehouseState

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _navigationState = SingleLiveEvent<CourierWarehousesNavigationState>()
    val navigationState: LiveData<CourierWarehousesNavigationState>
        get() = _navigationState

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _showOrdersState = MutableLiveData<CourierWarehousesShowOrdersState>()
    val showOrdersState: LiveData<CourierWarehousesShowOrdersState>
        get() = _showOrdersState

    private val _demoState = MutableLiveData<Boolean>()
    val demoState: LiveData<Boolean>
        get() = _demoState

    private var warehouseEntities = mutableListOf<CourierWarehouseLocalEntity>()
    private var warehouseItems = mutableListOf<CourierWarehouseItem>()
    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var coordinatePoints = mutableListOf<CoordinatePoint>()
    private lateinit var myLocation: CoordinatePoint

    private var whSelectedId: Int? = null

    init {
        checkDemoMode()
    }

    private fun checkDemoMode() {
        _demoState.value = interactor.isDemoMode()
    }

    private fun observeMapAction() {
        addSubscription(
            interactor.observeMapAction()
                .subscribe(
                    { observeMapActionComplete(it) },
                    { observeMapActionError(it) }
                ))
    }

    private fun observeMapActionComplete(it: CourierMapAction) {
        when (it) {
            is CourierMapAction.ItemClick -> onMapPointClick(it.point)
            is CourierMapAction.LocationUpdate -> initMapByLocation(it.point)
            CourierMapAction.MapClick -> {}
        }
    }

    private fun observeMapActionError(throwable: Throwable) {
        onTechErrorLog("observeMapActionError", throwable)
    }

    fun updateData() {
        observeMapAction()
        getWarehouses()
    }


    fun toRegistrationClick() {
        _navigationState.value = CourierWarehousesNavigationState.NavigateToRegistration
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private fun getWarehouses() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.getWarehouses()
                .doFinally { clearFabAndWhList() }
                .subscribe(
                    {
                        sortedWarehouseEntities(it)
                        convertAndSaveItemsPointsMarkers(warehouseEntities)
                        courierWarehouseComplete()
                        setLoader(WaitLoader.Complete)
                    },
                    {
                        onTechErrorLog("courierWarehouseError", it)

                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                        if (warehouseItems.isEmpty()) {
                            _warehouseState.value =
                                CourierWarehouseItemState.Empty("Ошибка получения данных")
                        }
                        setLoader(WaitLoader.Complete)
                    })
        )
    }

    private fun sortedWarehouseEntities(it: List<CourierWarehouseLocalEntity>) {
        warehouseEntities = it.sortedBy { warehouse -> warehouse.name }.toMutableList()
    }

    private fun convertAndSaveItemsPointsMarkers(warehouses: List<CourierWarehouseLocalEntity>) {
        onTechEventLog("courierWarehouseComplete", "warehouses count " + warehouses.size)
        warehouseItems = mutableListOf()
        coordinatePoints = mutableListOf()
        mapMarkers = mutableListOf()
        warehouseEntities.forEachIndexed { index, item ->
            val wi = CourierWarehouseItem(item.id, item.name, item.fullAddress, false)
            warehouseItems.add(wi)
            coordinatePoints.add(CoordinatePoint(item.latitude, item.longitude))
            val mapPoint = MapPoint(index.toString(), item.latitude, item.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getWarehouseMapIcon())
            mapMarkers.add(mapMarker)
        }

    }

    private fun courierWarehouseComplete() {
        interactor.mapState(CourierMapState.UpdateMyLocation)
        _warehouseState.value =
            if (warehouseItems.isEmpty()) {
                CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            } else {
                CourierWarehouseItemState.InitItems(warehouseItems.toMutableList())
            }
    }

    private fun initMapByLocation(location: CoordinatePoint) {
        onTechEventLog("initMapByLocation")
        myLocation = location
        if (coordinatePoints.isEmpty()) {
            interactor.mapState(CourierMapState.NavigateToMyLocation)
        } else {
            updateMarkersWithMyLocation(location)
            zoomMarkersFromBoundingBox(location)
        }
    }

    private fun updateMarkersWithMyLocation(myLocation: CoordinatePoint) {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
    }

    private fun zoomMarkersFromBoundingBox(myLocation: CoordinatePoint) {
        if (coordinatePoints.isNotEmpty()) {
            val boundingBox = MapEnclosingCircle().minimumBoundingBoxRelativelyMyLocation(
                coordinatePoints, myLocation, RADIUS_KM
            )
            interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
        } else {
            interactor.mapState(CourierMapState.NavigateToPoint(myLocation))
        }
    }

    private fun onMapPointClick(mapPoint: MapPoint) {
        onTechEventLog("onItemPointClick")
        if (mapPoint.id != MY_LOCATION_ID) {
            val indexItemClick = mapPoint.id.toInt()
            changeSelectedMapPoint(mapPoint)
            updateMarkers()
            val isMapSelected = isMapSelected(indexItemClick)
            changeSelectedWarehouseItemsByMap(indexItemClick, isMapSelected)
            updateAndScrollToItems(indexItemClick)
            changeShowOrders(isMapSelected)
        }
    }

    private fun changeSelectedMapPoint(mapPoint: MapPoint) {
        mapMarkers.forEach { item ->
            item.icon =
                if (item.point.id == mapPoint.id &&
                    item.icon == resourceProvider.getWarehouseMapIcon()
                )
                    resourceProvider.getWarehouseMapSelectedIcon()
                else resourceProvider.getWarehouseMapIcon()
        }
    }

    private fun changeSelectedWarehouseItemsByMap(indexItemClick: Int, isMapSelected: Boolean) {

        warehouseItems[indexItemClick].isSelected = isMapSelected
        if (whSelectedId != null && whSelectedId != indexItemClick) {
            warehouseItems[whSelectedId!!].isSelected = false
        }
        whSelectedId = if (isMapSelected) indexItemClick else null

    }

    private fun updateAndScrollToItems(indexItemClick: Int) {
        _warehouseState.value =
            CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList())
        _warehouseState.value = CourierWarehouseItemState.ScrollTo(indexItemClick)
    }

    private fun isMapSelected(indexItemClick: Int) =
        mapMarkers[indexItemClick].icon == resourceProvider.getWarehouseMapSelectedIcon()

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
    }

    fun onItemClick(index: Int) {
        onTechEventLog("onItemClick", "index $index")
        val isSelected = !warehouseItems[index].isSelected
        changeMapMarkers(index, isSelected)
        changeWarehouseItems(index, isSelected)
        changeShowOrders(isSelected)
    }

    private fun changeMapMarkers(clickItemIndex: Int, isSelected: Boolean) {
        mapMarkers.forEachIndexed { index, item ->
            item.icon = if (index == clickItemIndex) {
                if (isSelected) resourceProvider.getWarehouseMapSelectedIcon()
                else resourceProvider.getWarehouseMapIcon()
            } else {
                resourceProvider.getWarehouseMapIcon()
            }
        }
        updateMarkersWithMyLocation(myLocation)
        if (isSelected) {
            with(mapMarkers[clickItemIndex].point) {
                val coordinatePoint = CoordinatePoint(lat, long)
                interactor.mapState(CourierMapState.NavigateToPoint(coordinatePoint))
            }
        }
    }

    private fun changeShowOrders(selected: Boolean) {
        _showOrdersState.value =
            if (selected) CourierWarehousesShowOrdersState.Enable
            else CourierWarehousesShowOrdersState.Disable
    }

    private fun changeWarehouseItems(selectIndex: Int, isSelected: Boolean) {
        changeSelectedWarehouseItemsByMap(selectIndex, isSelected)
        _warehouseState.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList())
    }

    private fun navigateToCourierOrders(oldEntity: CourierWarehouseLocalEntity) {
//        clearSubscription()
        _navigationState.value = CourierWarehousesNavigationState.NavigateToCourierOrders(
            oldEntity.id,
            oldEntity.latitude,
            oldEntity.longitude,
            oldEntity.name
        )
    }

    fun onNextFab() {
        val index = warehouseItems.indexOfFirst { item -> item.isSelected }
        assert(index != -1)
        clearFabAndWhList()
        val oldEntity = warehouseEntities[index].copy()
        interactor.clearAndSaveCurrentWarehouses(oldEntity).subscribe()
        navigateToCourierOrders(oldEntity)
        clearSubscription()
    }

    fun onShowAllClick() {
        zoomMarkersFromBoundingBox(myLocation)
    }

    private fun clearFabAndWhList() {
        whSelectedId = null
        changeShowOrders(false)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierWarehouses"
        const val RADIUS_KM = 30
    }

}