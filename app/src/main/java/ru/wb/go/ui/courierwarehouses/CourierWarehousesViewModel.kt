package ru.wb.go.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehouseInteractor
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
    private val interactor: CourierWarehouseInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _warehouses = MutableLiveData<CourierWarehouseItemState>()
    val warehouses: LiveData<CourierWarehouseItemState>
        get() = _warehouses

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

    init {
        onTechEventLog("init", "init CourierWarehousesViewModel")
    }

    private var warehouseEntities = mutableListOf<CourierWarehouseLocalEntity>()
    private var warehouseItems = mutableListOf<CourierWarehouseItem>()
    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var coordinatePoints = mutableListOf<CoordinatePoint>()
    private lateinit var myLocation: CoordinatePoint

    private fun saveWarehouseEntities(warehouseEntities: List<CourierWarehouseLocalEntity>) {
        this.warehouseEntities = warehouseEntities.toMutableList()
    }

    private fun saveCoordinatePoints(mapMarkers: List<CoordinatePoint>) {
        this.coordinatePoints = mapMarkers.toMutableList()
    }

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

    private fun observeMapAction() {
        addSubscription(
            interactor.observeMapAction()
                .subscribe(
                    { observeMapActionComplete(it) },
                    { onTechErrorLog("observeMapActionError", it)}
                ))
    }

    private fun observeMapActionComplete(it: CourierMapAction?) {
        when (it) {
            is CourierMapAction.ItemClick -> {
            }
            CourierMapAction.PermissionComplete -> {
                onTechEventLog("observeMapActionComplete", "PermissionComplete")
                getWarehouse()
            }
            is CourierMapAction.AutomatedLocationUpdate -> {
            }
            is CourierMapAction.ForcedLocationUpdate -> initMapByLocation(it.point)
            is CourierMapAction.PermissionDenied -> initMapByLocation(it.point)
        }
    }


    fun update() {
        observeMapAction()
        getWarehouse()
    }

    fun onUpdateClick() {
        observeMapAction()
        getWarehouse()
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private fun getWarehouse() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.getServerWarehouses()
                .doOnSuccess { saveWarehouseEntities(it) }
                .subscribe(
                    { courierWarehouseComplete(it) },
                    {
                        onTechErrorLog("courierWarehouseError", it)
                        errorDialogManager.showErrorDialog(it,_navigateToDialogInfo)
                        if (warehouseItems.isEmpty()) {
                            _warehouses.value = CourierWarehouseItemState.Empty("Ошибка получения данных")
                        }
                        setLoader(WaitLoader.Complete)
                    })
        )
    }

    private fun courierWarehouseComplete(warehouses: List<CourierWarehouseLocalEntity>) {
        onTechEventLog("courierWarehouseComplete", "warehouses count " + warehouses.size)
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
        warehouseItems.sortBy { w -> w.fullAddress }
        saveWarehouseItems(warehouseItems)
        initItems(warehouseItems)

        saveCoordinatePoints(coordinatePoints)
        saveMapMarkers(mapMarkers)
        interactor.mapState(CourierMapState.UpdateMyLocation)
        setLoader(WaitLoader.Complete)
    }


    private fun initMapByLocation(myLocation: CoordinatePoint) {
        onTechEventLog("initMapByLocation")
        saveMyLocation(myLocation)
        if (coordinatePoints.isEmpty()) navigateToMyLocation()
        else {
            updateMarkersWithMyLocation(myLocation)
            zoomMarkersFromBoundingBox(myLocation)
        }
    }

    private fun saveMyLocation(myLocation: CoordinatePoint) {
        this.myLocation = myLocation
    }

    private fun zoomMarkersFromBoundingBox(myLocation: CoordinatePoint) {
        val boundingBox = MapEnclosingCircle().minimumBoundingBoxRelativelyMyLocation(
            coordinatePoints, myLocation, RADIUS_KM
        )
        interactor.mapState(CourierMapState.ZoomToCenterBoundingBox(boundingBox))
    }

    private fun updateMarkersWithMyLocation(myLocation: CoordinatePoint) {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
    }

    private fun navigateToMyLocation() {
        interactor.mapState(CourierMapState.NavigateToMyLocation)
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
        onTechEventLog("onItemClick", "index $index")
        changeItemSelected(index)
    }

    private fun changeItemSelected(selectIndex: Int) {
        val isSelected = changeWarehouseItems(selectIndex)
        changeMapMarkers(selectIndex, isSelected)
    }

    private fun changeWarehouseItems(selectIndex: Int): Boolean {
        warehouseItems.forEachIndexed { index, item ->
            warehouseItems[index].isSelected =
                if (selectIndex == index) !item.isSelected
                else false
        }
        _warehouses.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.UpdateItems(selectIndex, warehouseItems)
        return warehouseItems[selectIndex].isSelected
    }

    private fun changeMapMarkers(selectIndex: Int, isSelected: Boolean) {
        mapMarkers.forEach { item ->
            item.icon = if (item.point.id == selectIndex.toString()) {
                if (isSelected) resourceProvider.getWarehouseMapSelectedIcon()
                else resourceProvider.getWarehouseMapIcon()
            } else {
                resourceProvider.getWarehouseMapIcon()
            }
        }
        mapMarkers.find { it.point.id == selectIndex.toString() }?.apply {
            mapMarkers.remove(this)
            mapMarkers.add(this)
        }
        updateMarkersWithMyLocation(myLocation)
        if (isSelected) zoomMarkersFromBoundingBox(myLocation)
    }

    fun onDetailClick(index: Int) {

        val selectedWh = warehouseEntities[index].copy()

        interactor.clearAndSaveCurrentWarehouses(selectedWh)
            .andThen {
                _navigationState.value =
                    CourierWarehousesNavigationState.NavigateToCourierOrder(
                        selectedWh.id,
                        selectedWh.name
                    )
            }
            .subscribe()

        clearSubscription()
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierWarehouses"
        const val RADIUS_KM = 30
    }

}