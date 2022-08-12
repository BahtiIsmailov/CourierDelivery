package ru.wb.go.ui.courierwarehouses

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.wb.go.app.AppPreffsKeys.CLOSE_FRAGMENT_WHEN_ENDED_TIME
import ru.wb.go.app.AppPreffsKeys.FRAGMENT_MANAGER
import ru.wb.go.app.AppPreffsKeys.SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.exceptions.TimeoutException
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.MY_LOCATION_ID
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehousesInteractor
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import ru.wb.go.utils.prefs.SharedWorker
import java.net.UnknownHostException
import kotlin.math.roundToInt

class CourierWarehousesViewModel(
    private val interactor: CourierWarehousesInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
    private val sharedWorker: SharedWorker,
) : ServicesViewModel(interactor, resourceProvider) {

    private val _warehouseState = SingleLiveEvent<CourierWarehouseItemState>()
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

    private val _selectedMapPointForFragment = Channel<MapPoint>()
    val selectedMapPointForFragment = _selectedMapPointForFragment.receiveAsFlow()

    private var warehouseEntities = mutableSetOf<CourierWarehouseLocalEntity>()
    private var warehouseItems = mutableSetOf<CourierWarehouseItem>()
    private var mapMarkers = mutableSetOf<CourierMapMarker>()
    private var coordinatePoints = mutableListOf<CoordinatePoint>()
    private var myLocation: CoordinatePoint? = null
        get() = if (field == null) {
            CoordinatePoint(55.751244, 37.618423)
        } else {
            field
        }


    private var whSelectedId: Int? = null
    private var stringFromSms: String? = null



    fun resumeInit() {
        observeMapAction()
        checkDemoMode()
    }

    init {
        workWithSharedWorker()
    }

    private fun checkDemoMode() {
        _demoState.value = interactor.isDemoMode()
    }

    private fun workWithSharedWorker() {
        sharedWorker.saveMediate(CLOSE_FRAGMENT_WHEN_ENDED_TIME, "")
        if (sharedWorker.isAllExists(SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP)) {
            sharedWorker.delete(SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP)
        }
        stringFromSms = sharedWorker.load(FRAGMENT_MANAGER, "")
    }

    private fun observeMapAction() {
        viewModelScope.launch {
            interactor.observeMapAction()
                .collect {
                    try {
                        when (it) {
                            is CourierMapAction.ItemClick -> {
                                onMapPointClick(it.point)
                                _selectedMapPointForFragment.trySend(it.point)
                            }
                            is CourierMapAction.LocationUpdate -> {
                                initMapByLocation(it.point)
                            }
                            CourierMapAction.MapClick -> showManagerBar()
                            CourierMapAction.ShowAll -> onShowAllClick()
                            else -> {}
                        }
                    } catch (e: Exception) {
                        logException(e, "observeMapAction")
                    }

                }
        }

    }

    fun updateData() {
        getWarehouses()
    }

    fun toRegistrationClick() {
        _navigationState.value = CourierWarehousesNavigationState.NavigateToRegistration
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.value = state
    }

    private fun getWarehouses() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                interactor.deleteWarehouses()
                val response = interactor.getWarehouses()
                val warehousesLocalEntity = setDataForCourierWarehousesDataBase(response)
                _warehouseState.value = CourierWarehouseItemState.Success
                setLoader(WaitLoader.Complete)
                getWarehousesComplete(warehousesLocalEntity.toSet())
                warehousesLocalEntity.map {
                    interactor.saveWarehouses(it)
                }

            } catch (e: Exception) {
                logException(e, "getWarehouses")
                getWarehousesError(e)
            } finally {
                clearFabAndWhList()
            }
        }
    }


    private fun setDataForCourierWarehousesDataBase(courierWarehouseResponse: CourierWarehousesResponse): Set<CourierWarehouseLocalEntity> {
        courierWarehouseResponse.data.forEach {
            warehouseEntities.add(
                CourierWarehouseLocalEntity(
                    id = it.id,
                    name = it.name,
                    fullAddress = it.fullAddress,
                    longitude = it.long,
                    latitude = it.lat,
                    distanceFromUser = getDistanceFromUser(it.lat, it.long)
                )
            )
        }
        return warehouseEntities

    }


    private fun getDistanceFromUser(lat: Double, long: Double): Int {
        val loc1 = Location("")
        loc1.latitude = lat
        loc1.longitude = long
        val loc2 = Location("")
        loc2.latitude = myLocation!!.latitude
        loc2.longitude = myLocation!!.longitude
        return (loc1.distanceTo(loc2) / 1000).roundToInt()
    }

    private fun getWarehousesComplete(it: Set<CourierWarehouseLocalEntity>) {
        sortedWarehouseEntitiesByCourierLocation(it)
        convertAndSaveItemsPointsMarkers()
        updateMyLocation()
        courierWarehouseComplete()
    }


    private fun getWarehousesError(it: Throwable) {
        setLoader(WaitLoader.Complete)
        if (it is NoInternetException || it is TimeoutException || it is UnknownHostException) {
            _warehouseState.value = CourierWarehouseItemState.NoInternet
        } else {
            errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
            _warehouseState.value = CourierWarehouseItemState.Empty("Ошибка получения данных")
        }
    }


    private fun sortedWarehouseEntitiesByCourierLocation(it: Set<CourierWarehouseLocalEntity>) {
        warehouseEntities = it.sortedBy { warehouse -> warehouse.distanceFromUser }.toMutableSet()
    }

    private fun convertAndSaveItemsPointsMarkers() {
        warehouseItems = mutableSetOf()
        coordinatePoints = mutableListOf()
        mapMarkers = mutableSetOf()
        warehouseEntities.forEachIndexed { index, item ->
            val wi = CourierWarehouseItem(item.id, item.name, item.fullAddress, false)
            warehouseItems.add(wi)
            coordinatePoints.add(
                CoordinatePoint(item.latitude, item.longitude)
            )
            val mapPoint = MapPoint(
                index.toString(),
                item.latitude,
                item.longitude
            )
            val mapMarker = Empty(mapPoint, resourceProvider.getWarehouseMapIcon())
            mapMarkers.add(mapMarker)
        }
    }

    private fun courierWarehouseComplete() {
        _warehouseState.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.InitItems(
                warehouseItems
            )
    }

    private fun showManagerBar() {
        interactor.mapState(CourierMapState.ShowManagerBar)
    }

    private fun updateMyLocation() {
        interactor.mapState(CourierMapState.UpdateMyLocation)
    }

    private fun initMapByLocation(location: CoordinatePoint) {
        myLocation = location
        if (coordinatePoints.isEmpty()) {
            interactor.mapState(CourierMapState.NavigateToMyLocation)
        } else {
            updateMarkersWithMyLocation(location)
            zoomMarkersFromBoundingBox(location)
        }

    }


    private fun updateMarkersWithMyLocation(myLocation: CoordinatePoint) {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers.toMutableList()))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))

    }

    private fun zoomMarkersFromBoundingBox(myLocation: CoordinatePoint) { // приближает к маркеру
        if (coordinatePoints.isNotEmpty()) {
            val boundingBox = MapEnclosingCircle().minimumBoundingBoxRelativelyMyLocation(
                coordinatePoints, myLocation, RADIUS_KM
            )
            interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
        } else {
            interactor.mapState(CourierMapState.NavigateToPoint(myLocation))
        }

    }

    fun onMapPointClick(mapPoint: MapPoint) {
        viewModelScope.launch {
            if (mapPoint.id != MY_LOCATION_ID) {

                //zoomMarkersFromBoundingBox(CoordinatePoint(mapPoint.lat,mapPoint.long))
                val indexItemClick = mapPoint.id.toInt()
                changeSelectedMapPoint(mapPoint)
                updateMarkers()
                val isMapSelected = isMapSelected(indexItemClick)
                changeSelectedWarehouseItemsByMap(indexItemClick, isMapSelected)
                updateAndScrollToItems(indexItemClick)
                val currentWarehouse = interactor.loadWarehousesFromId(warehouseItems.elementAt(indexItemClick).id)
                changeShowDetailsOrder(isMapSelected,currentWarehouse)
            }
        }
    }

    private fun changeSelectedMapPoint(mapPoint: MapPoint) {
        mapMarkers.forEach { item ->
            item.icon =
                if (item.point.id == mapPoint.id &&
                    item.icon == resourceProvider.getWarehouseMapIcon()
                ) {
                    interactor.mapState(CourierMapState.NavigateToPoint(CoordinatePoint(mapPoint.lat,mapPoint.long)))
                    resourceProvider.getWarehouseMapSelectedIcon()
                }
                else if(item.point.id == mapPoint.id &&
                    item.icon == resourceProvider.getWarehouseMapSelectedIcon()){
                    resourceProvider.getWarehouseMapIcon()
                }
                else {
                    resourceProvider.getWarehouseMapIcon()
                }
        }
    }

    private fun changeSelectedWarehouseItemsByMap(indexItemClick: Int, isMapSelected: Boolean) {
        warehouseItems.elementAt(indexItemClick).isSelected = isMapSelected
        if (whSelectedId != null && whSelectedId != indexItemClick) {
            warehouseItems.elementAt(whSelectedId!!).isSelected = false
        }
        whSelectedId = if (isMapSelected) indexItemClick else null
    }

    private fun updateAndScrollToItems(indexItemClick: Int) {
        updateItems()
        _warehouseState.value = CourierWarehouseItemState.ScrollTo(indexItemClick)
    }

    private fun isMapSelected(indexItemClick: Int) =
        mapMarkers.elementAt(indexItemClick).icon == resourceProvider.getWarehouseMapSelectedIcon()

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers.toMutableList()))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation!!))
    }

    fun onItemClick(index: Int) {
        viewModelScope.launch {
            val isSelected = !warehouseItems.elementAt(index).isSelected
            changeMapMarkers(index, isSelected)
            changeWarehouseItems(index, isSelected)
            val currentWarehouse = interactor.loadWarehousesFromId(warehouseItems.elementAt(index).id)
            changeShowDetailsOrder(isSelected,currentWarehouse)
        }
    }

    private fun changeMapMarkers(clickItemIndex: Int, isSelected: Boolean) {
        mapMarkers.forEachIndexed { index, item ->
            item.icon = if (index == clickItemIndex) {
                if (isSelected) {
                    resourceProvider.getWarehouseMapSelectedIcon()
                } else {
                    resourceProvider.getWarehouseMapIcon()
                }
            } else {
                resourceProvider.getWarehouseMapIcon()
            }
        }
        updateMarkersWithMyLocation(myLocation!!)
        if (isSelected) {
            with(mapMarkers.elementAt(clickItemIndex).point) {
                val coordinatePoint = CoordinatePoint(lat, long)
                interactor.mapState(CourierMapState.NavigateToPoint(coordinatePoint))
            }
        }
    }

    private fun changeShowDetailsOrder(selected: Boolean, warehouseItem:List<CourierWarehouseLocalEntity>?) {
        if (selected){
            val distance = resourceProvider.getDistance(warehouseItem?.get(0)?.distanceFromUser.toString())
            _showOrdersState.value = CourierWarehousesShowOrdersState.Enable(warehouseItem, distance)
        }
        else {
            _showOrdersState.value = CourierWarehousesShowOrdersState.Disable
        }
    }

    private fun changeWarehouseItems(selectIndex: Int, isSelected: Boolean) {
        changeSelectedWarehouseItemsByMap(selectIndex, isSelected)
        if (warehouseItems.isEmpty()) {
            _warehouseState.value = CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
        } else {
            _warehouseState.value =
                CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList())
        }
    }

    private fun updateItems() {
        _warehouseState.value =
            CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList())
    }


    fun onNextFab() {
        viewModelScope.launch {
            val index = warehouseItems.indexOfFirst { item ->
                item.isSelected
            }
            assert(index != -1)
            clearFabAndWhList()
            val oldEntity = warehouseEntities.elementAt(index).copy()
            interactor.clearAndSaveCurrentWarehouses(oldEntity)
            navigateToCourierOrders(oldEntity)
        }
    }

    private fun navigateToCourierOrders(oldEntity: CourierWarehouseLocalEntity) {
        _navigationState.value =
            CourierWarehousesNavigationState.NavigateToCourierOrders(
                oldEntity.id,
                oldEntity.latitude,
                oldEntity.longitude,
                oldEntity.name
            )
    }


    private fun onShowAllClick() {
        zoomMarkersFromBoundingBox(myLocation!!)
    }

    private fun clearFabAndWhList() {
        whSelectedId = null
        changeShowDetailsOrder(false,null)
//        if (stringFromSms == "") {
//            interactor.clearCacheMutableSharedFlow()
//        }else{
//            sharedWorker.saveMediate(FRAGMENT_MANAGER,"")
//            stringFromSms = ""
//        }
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierWarehouses"
        const val RADIUS_KM = 30
    }

}

