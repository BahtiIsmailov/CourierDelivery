package ru.wb.go.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.ServicesViewModel
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
) : ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

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

    private var warehouseEntities = mutableListOf<CourierWarehouseLocalEntity>()
    private var warehouseItems = mutableListOf<CourierWarehouseItem>()
    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var coordinatePoints = mutableListOf<CoordinatePoint>()
    private lateinit var myLocation: CoordinatePoint



    private var whSelectedId: Int? = null

    fun resumeInit() {
        checkDemoMode()
        observeMapAction()
    }

    private fun checkDemoMode() {
        _demoState.postValue(interactor.isDemoMode())
    }
    private fun observeMapAction() {
        viewModelScope.launch {
            try {
                interactor.observeMapAction().onEach {
                    observeMapActionComplete(it)
                }
            }catch (e:Exception){
                observeMapActionError(e)
            }
        }

    }

//    private fun observeMapAction() {
//        addSubscription(
//            interactor.observeMapAction()
//                .subscribe(
//                    { observeMapActionComplete(it) },
//                    { observeMapActionError(it) }
//                ))
//    }

    private fun observeMapActionComplete(it: CourierMapAction) {
        when (it) {
            is CourierMapAction.ItemClick -> onMapPointClick(it.point)
            is CourierMapAction.LocationUpdate -> initMapByLocation(it.point)
            is CourierMapAction.MapClick -> showManagerBar()
            is CourierMapAction.ShowAll -> onShowAllClick()
            else -> {}
        }
    }

    private fun observeMapActionError(throwable: Throwable) {
        onTechErrorLog("observeMapActionError", throwable)
    }

     fun updateData() {
         viewModelScope.launch {
             getWarehouses()
         }
    }

    fun toRegistrationClick() {
        _navigationState.postValue(CourierWarehousesNavigationState.NavigateToRegistration)
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private suspend fun getWarehouses() {
        setLoader(WaitLoader.Wait)
        val job = viewModelScope.launch {
            try {
                val response = interactor.getWarehouses()
                getWarehousesComplete(response)
            } catch (e: Exception) {
                getWarehousesError(e)
            }
        }
        job.join()
        clearFabAndWhList()
    }

    private fun getWarehousesComplete(it: List<CourierWarehouseLocalEntity>) {
        sortedWarehouseEntities(it)
        convertAndSaveItemsPointsMarkers()
        updateMyLocation()
        courierWarehouseComplete()
        setLoader(WaitLoader.Complete)
    }

    private fun getWarehousesError(it: Throwable) {
        onTechErrorLog("courierWarehouseError", it)
        setLoader(WaitLoader.Complete)
        if (it is NoInternetException) {
            _warehouseState.postValue(CourierWarehouseItemState.NoInternet)
        } else {
            errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
            _warehouseState.postValue(CourierWarehouseItemState.Empty("Ошибка получения данных"))
        }
    }

    private fun sortedWarehouseEntities(it: List<CourierWarehouseLocalEntity>) {
        warehouseEntities = it.sortedBy { warehouse -> warehouse.name }.toMutableList()
    }

    private fun convertAndSaveItemsPointsMarkers() {
        onTechEventLog("courierWarehouseComplete", "warehouses count " + warehouseEntities.size)
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
        if (warehouseItems.isEmpty()){
            _warehouseState.postValue(CourierWarehouseItemState.Empty(resourceProvider.getEmptyList()))
        }else{
            _warehouseState.postValue(CourierWarehouseItemState.InitItems(warehouseItems.toMutableList()))
        }
    }

    private fun showManagerBar() {
        viewModelScope.launch {
            interactor.mapState(CourierMapState.ShowManagerBar)
        }

    }

    private fun updateMyLocation() {
        viewModelScope.launch {
            interactor.mapState(CourierMapState.UpdateMyLocation)
        }
    }

    private fun initMapByLocation(location: CoordinatePoint) {
        viewModelScope.launch {
            onTechEventLog("initMapByLocation")
            myLocation = location
            if (coordinatePoints.isEmpty()) {
                interactor.mapState(CourierMapState.NavigateToMyLocation)
            } else {
                updateMarkersWithMyLocation(location)
                zoomMarkersFromBoundingBox(location)
            }
        }

    }

    private fun updateMarkersWithMyLocation(myLocation: CoordinatePoint) {
        viewModelScope.launch {
            interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
            interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
        }

    }

    private fun zoomMarkersFromBoundingBox(myLocation: CoordinatePoint) {
        viewModelScope.launch {
            if (coordinatePoints.isNotEmpty()) {
                val boundingBox = MapEnclosingCircle().minimumBoundingBoxRelativelyMyLocation(
                    coordinatePoints, myLocation, RADIUS_KM
                )
                interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
            } else {
                interactor.mapState(CourierMapState.NavigateToPoint(myLocation))
            }
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
            changeShowDetailsOrder(isMapSelected)
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
        updateItems()
        _warehouseState.postValue(CourierWarehouseItemState.ScrollTo(indexItemClick))
    }

    private fun isMapSelected(indexItemClick: Int) =
        mapMarkers[indexItemClick].icon == resourceProvider.getWarehouseMapSelectedIcon()

    private fun updateMarkers() {
        viewModelScope.launch {
            interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
            interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
        }

    }

    fun onItemClick(index: Int) {
        onTechEventLog("onItemClick", "index $index")
        val isSelected = !warehouseItems[index].isSelected
        changeMapMarkers(index, isSelected)
        changeWarehouseItems(index, isSelected)
        changeShowDetailsOrder(isSelected)
    }

    private fun changeMapMarkers(clickItemIndex: Int, isSelected: Boolean) {
        viewModelScope.launch {
            mapMarkers.forEachIndexed { index, item ->
                item.icon = if (index == clickItemIndex) {
                    if (isSelected) {
                        resourceProvider.getWarehouseMapSelectedIcon()
                    }
                    else {
                        resourceProvider.getWarehouseMapIcon()
                    }
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

    }

    private fun changeShowDetailsOrder(selected: Boolean) {
        _showOrdersState.postValue(
            if (selected) CourierWarehousesShowOrdersState.Enable
            else CourierWarehousesShowOrdersState.Disable)
    }

    private fun changeWarehouseItems(selectIndex: Int, isSelected: Boolean) {
        changeSelectedWarehouseItemsByMap(selectIndex, isSelected)
        if (warehouseItems.isEmpty()){
            _warehouseState.postValue(CourierWarehouseItemState.Empty(resourceProvider.getEmptyList()))
        }
        else {
            _warehouseState.postValue(CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList()))
        }
    }

    private fun updateItems() {
        _warehouseState.postValue(CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList()))
    }

    private fun navigateToCourierOrders(oldEntity: CourierWarehouseLocalEntity) {
        _navigationState.postValue(CourierWarehousesNavigationState.NavigateToCourierOrders(
            oldEntity.id,
            oldEntity.latitude,
            oldEntity.longitude,
            oldEntity.name
        ))
    }

    fun onNextFab() {
        viewModelScope.launch {
            val index = warehouseItems.indexOfFirst { item -> item.isSelected }
            assert(index != -1)
            clearFabAndWhList()
            val oldEntity = warehouseEntities[index].copy()
            interactor.clearAndSaveCurrentWarehouses(oldEntity)
            navigateToCourierOrders(oldEntity)
            clearSubscription()
        }

    }

    private fun onShowAllClick() {
        zoomMarkersFromBoundingBox(myLocation)
    }

    private fun clearFabAndWhList() {
        whSelectedId = null
        changeShowDetailsOrder(false)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierWarehouses"
        const val RADIUS_KM = 30
    }

}