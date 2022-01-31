package ru.wb.go.ui.courierwarehouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.MY_LOCATION_ID
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehouseInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint

class CourierWarehousesViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierWarehouseInteractor,
    private val resourceProvider: CourierWarehousesResourceProvider,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _warehouseItems = MutableLiveData<CourierWarehouseItemState>()
    val warehouses: LiveData<CourierWarehouseItemState>
        get() = _warehouseItems

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _navigationState = SingleLiveEvent<CourierWarehousesNavigationState>()
    val navigationState: LiveData<CourierWarehousesNavigationState>
        get() = _navigationState

    private val _warehousesProgressState = MutableLiveData<CourierWarehousesProgressState>()
    val warehousesProgressState: LiveData<CourierWarehousesProgressState>
        get() = _warehousesProgressState

    private val _holdState = MutableLiveData<Boolean>()
    val holdState: LiveData<Boolean>
        get() = _holdState

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

    init {
        onTechEventLog("init", "init CourierWarehousesViewModel")
    }

    fun update() {
        changeDemoMode()
        observeMapAction()
        getWarehouses()
    }

    private fun changeDemoMode() {
        _demoState.value = interactor.isDemoMode()
    }

    private fun observeMapAction() {
        addSubscription(
            interactor.observeMapAction().subscribe(
                { observeMapActionComplete(it) },
                { observeMapActionError(it) }
            ))
    }

    private fun observeMapActionComplete(it: CourierMapAction) {
        when (it) {
            is CourierMapAction.ItemClick -> onMapPointClick(it.point)
            CourierMapAction.PermissionComplete -> {
                onTechEventLog("observeMapActionComplete", "PermissionComplete")
                getWarehouses()
            }
            is CourierMapAction.AutomatedLocationUpdate -> {
            }
            is CourierMapAction.ForcedLocationUpdate -> initMapByLocation(it.point)
            is CourierMapAction.PermissionDenied -> initMapByLocation(it.point)
            CourierMapAction.MapClick -> {}
        }
    }

    private fun observeMapActionError(throwable: Throwable) {
        onTechErrorLog("observeMapActionError", throwable)
    }

    fun toRegistrationClick() {

    }

    private fun lockState() {
        _holdState.value = true
    }

    private fun unlockState() {
        _holdState.value = false
    }

    private fun getWarehouses() {
        addSubscription(
            interactor.getServerWarehouses()
                // TODO: 13.01.2022 для тестирования
//                .map {
//                    val test1 = CourierWarehouseLocalEntity(
//                        1001,
//                        "test1",
//                        "address test 1",
//                        38.618423,
//                        56.751244
//                    )
//                    val test2 = CourierWarehouseLocalEntity(
//                        1012,
//                        "test2",
//                        "address test 2",
//                        36.618423,
//                        54.751244
//                    )
//                    val test3 = CourierWarehouseLocalEntity(
//                        1014,
//                        "test3",
//                        "address test 3",
//                        39.618423,
//                        52.751244
//                    )
//                    val test4 = CourierWarehouseLocalEntity(
//                        1017,
//                        "test4",
//                        "address test 4",
//                        40.618423,
//                        53.751244
//                    )
//                    val test5 = CourierWarehouseLocalEntity(
//                        1019,
//                        "test5",
//                        "address test 5",
//                        38.618423,
//                        50.751244
//                    )
//                    val test6 = CourierWarehouseLocalEntity(
//                        1020,
//                        "test6",
//                        "address test 6",
//                        41.618423,
//                        54.751244
//                    )
//                    mutableListOf(test1, test2, test3, test4, test5, test6)
//                }
                .doOnSuccess { saveWarehouseEntities(it) }
                .doOnSuccess { convertAndSaveItemsPointsMarkers(it) }
                .subscribe(
                    { courierWarehouseComplete() },
                    { courierWarehouseError(it) })
        )
    }

    private fun convertAndSaveItemsPointsMarkers(warehouses: List<CourierWarehouseLocalEntity>) {
        onTechEventLog("courierWarehouseComplete", "warehouses count " + warehouses.size)
        val warehouseItems = mutableListOf<CourierWarehouseItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val mapMarkers = mutableListOf<CourierMapMarker>()
        warehouses.forEachIndexed { index, item ->
            val warehouseItem = CourierWarehouseItem(item.id, item.name, item.fullAddress, false)
            warehouseItems.add(warehouseItem)
            val coordinatePoint = CoordinatePoint(item.latitude, item.longitude)
            coordinatePoints.add(coordinatePoint)
            val mapPoint = MapPoint(index.toString(), item.latitude, item.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getWarehouseMapIcon())
            mapMarkers.add(mapMarker)
        }
        saveWarehouseItems(warehouseItems)
        saveCoordinatePoints(coordinatePoints)
        saveMapMarkers(mapMarkers)
    }

    private fun saveWarehouseEntities(warehouseEntities: List<CourierWarehouseLocalEntity>) {
        this.warehouseEntities = warehouseEntities.toMutableList()
    }

    private fun saveCoordinatePoints(mapMarkers: List<CoordinatePoint>) {
        this.coordinatePoints = mapMarkers.toMutableList()
    }

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

    private fun courierWarehouseComplete() {
        updateMyLocation()
        initWarehouseItems(warehouseItems)
        requestFinishUnlockState()
    }

    private fun updateMyLocation() {
        interactor.mapState(CourierMapState.UpdateMyLocation)
    }

    private fun requestFinishUnlockState() {
        progressComplete()
        unlockState()
    }

    private fun initMapByLocation(myLocation: CoordinatePoint) {
        onTechEventLog("initMapByLocation")
        saveMyLocation(myLocation)
        if (coordinatePoints.isEmpty()) navigateToMyLocation()
        else {
            updateMarkersWithMyLocation(myLocation)
            zoomMarkersFromBoundingBox(myLocation)
        }
        requestFinishUnlockState()
    }

    private fun saveMyLocation(myLocation: CoordinatePoint) {
        this.myLocation = myLocation
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

    private fun navigateToMyLocation() {
        interactor.mapState(CourierMapState.NavigateToMyLocation)
    }

    private fun courierWarehouseError(throwable: Throwable) {
        onTechErrorLog("courierWarehouseError", throwable)
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.error.message,
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigateToDialogInfo.value = message
        if (warehouseItems.isEmpty()) {
            _warehouseItems.value = CourierWarehouseItemState.Empty(message.title)
        }
        requestFinishUnlockState()
    }

    private fun progressComplete() {
        _warehousesProgressState.value = CourierWarehousesProgressState.ProgressComplete
    }

    private fun initWarehouseItems(warehouseItems: MutableList<CourierWarehouseItem>) {
        _warehouseItems.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.InitItems(warehouseItems)
    }

    private fun saveWarehouseItems(warehouses: List<CourierWarehouseItem>) {
        warehouseItems = warehouses.toMutableList()
    }

    private fun onMapPointClick(mapPoint: MapPoint) {
        onTechEventLog("onItemPointClick")
        if (mapPoint.id == MY_LOCATION_ID) {
        } else {
            val indexItemClick = mapPoint.id.toInt()
            changeSelectedMapPoint(mapPoint)
            updateMarkers()
            val isMapSelected = isMapSelected(indexItemClick)
            changeSelectedWarehouseItems(indexItemClick, isMapSelected)
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

    private fun changeSelectedWarehouseItems(indexItemClick: Int, isMapSelected: Boolean) {
        warehouseItems.forEachIndexed { index, courierWarehouseItem ->
            courierWarehouseItem.isSelected =
                if (index == indexItemClick) isMapSelected
                else false
        }
    }

    private fun updateAndScrollToItems(indexItemClick: Int) {
        _warehouseItems.value =
            CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList())
        _warehouseItems.value = CourierWarehouseItemState.ScrollTo(indexItemClick)
    }

    private fun isMapSelected(indexItemClick: Int) =
        mapMarkers[indexItemClick].icon == resourceProvider.getWarehouseMapSelectedIcon()

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
    }

    fun onItemClick(index: Int) {
        onTechEventLog("onItemClick", "index $index")
        changeItemSelected(index)
    }

    private fun changeItemSelected(clickItemIndex: Int) {
        val isSelected = isInvertWarehouseItemsSelected(clickItemIndex)
        changeMapMarkers(clickItemIndex, isSelected)
        changeWarehouseItems(clickItemIndex, isSelected)
        changeShowOrders(isSelected)
    }

    private fun isInvertWarehouseItemsSelected(selectIndex: Int) =
        !warehouseItems[selectIndex].isSelected

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
            if (selected) CourierWarehousesShowOrdersState.Enable else CourierWarehousesShowOrdersState.Disable
    }

    private fun changeWarehouseItems(selectIndex: Int, isSelected: Boolean) {
        warehouseItems.forEachIndexed { index, item ->
            item.isSelected =
                if (selectIndex == index) isSelected
                else false
        }
        _warehouseItems.value =
            if (warehouseItems.isEmpty()) CourierWarehouseItemState.Empty(resourceProvider.getEmptyList())
            else CourierWarehouseItemState.UpdateItems(warehouseItems.toMutableList())
    }

    private fun checkAndNavigate(
        warehouseEntities: List<CourierWarehouseLocalEntity>,
        oldEntity: CourierWarehouseLocalEntity
    ) {
        _showOrdersState.value = CourierWarehousesShowOrdersState.Disable
        val idWarehouseFound = warehouseEntities.find { it.id == oldEntity.id }
        if (idWarehouseFound == null) {
            convertAndSaveItemsPointsMarkers(warehouseEntities)
            courierWarehouseComplete()
            showWarehouseOrdersIsNotExistDialog()
        } else {
            interactor.clearAndSaveCurrentWarehouses(oldEntity).subscribe()
            navigateToCourierOrder(oldEntity)
            clearSubscription()
        }
        requestFinishUnlockState()
    }

    private fun navigateToCourierOrder(oldEntity: CourierWarehouseLocalEntity) {
        _navigationState.value = CourierWarehousesNavigationState.NavigateToCourierOrder(
            oldEntity.id,
            oldEntity.latitude,
            oldEntity.longitude,
            oldEntity.name
        )
    }

    private fun showWarehouseOrdersIsNotExistDialog() {
        _navigateToDialogInfo.value =
            NavigateToDialogInfo(
                DialogInfoStyle.WARNING.ordinal,
                resourceProvider.getDialogEmptyTitle(),
                resourceProvider.getDialogEmptyMessage(),
                resourceProvider.getDialogEmptyButton(),
            )
    }

    fun onDetailClick() {
        run lit@{
            warehouseItems.forEachIndexed { index, item ->
                if (item.isSelected) {
                    checkAndNavigateToOrders(index)
                    return@lit
                }
            }
        }

    }

    fun onShowAllClick() {
        zoomMarkersFromBoundingBox(myLocation)
    }

    private fun checkAndNavigateToOrders(index: Int) {
        lockState()
        showProgress()
        onTechEventLog("onDetailClick", "index $index")
        val oldEntity = warehouseEntities[index].copy()
        addSubscription(
            interactor.getServerWarehouses()
                .doOnSuccess { saveWarehouseEntities(it) }
                .subscribe(
                    { checkAndNavigate(it, oldEntity) },
                    { courierWarehouseError(it) })
        )
    }

    private fun showProgress() {
        _warehousesProgressState.value = CourierWarehousesProgressState.Progress
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