package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.WAREHOUSE_ID
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem
import ru.wb.go.ui.courierorders.domain.CourierOrderInteractor
import ru.wb.go.ui.courierwarehouses.CourierWarehousesShowOrdersState
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint

class CourierOrdersViewModel(
    private val parameters: CourierOrderParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrderInteractor,
    private val dataBuilder: CourierOrdersDataBuilder,
    private val resourceProvider: CourierOrdersResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

//    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
//    val toolbarNetworkState: LiveData<NetworkState>
//        get() = _toolbarNetworkState

//    private val _versionApp = MutableLiveData<String>()
//    val versionApp: LiveData<String>
//        get() = _versionApp

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _orders = MutableLiveData<CourierOrderItemState>()
    val orders: LiveData<CourierOrderItemState>
        get() = _orders

    private val _progressState = MutableLiveData<CourierOrdersProgressState>()
    val progressState: LiveData<CourierOrdersProgressState>
        get() = _progressState

    private val _navigationState = SingleLiveEvent<CourierOrdersNavigationState>()
    val navigationState: LiveData<CourierOrdersNavigationState>
        get() = _navigationState

    private val _holdState = MutableLiveData<Boolean>()
    val holdState: LiveData<Boolean>
        get() = _holdState

    private val _showOrdersState = MutableLiveData<CourierWarehousesShowOrdersState>()
    val showOrdersState: LiveData<CourierWarehousesShowOrdersState>
        get() = _showOrdersState

    private var orderEntities = mutableListOf<CourierOrderEntity>()
    private var orderItems = mutableListOf<BaseItem>()
    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var centerGroupPoints = mutableListOf<CoordinatePoint>()


    init {
        onTechEventLog("init")
//        observeNetworkState()
//        fetchVersionApp()
        observeMapAction()
        initToolbarLabel()
        initOrders()
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
            else -> {}
        }
    }

    private fun onMapPointClick(mapPoint: MapPoint) {
        onTechEventLog("onItemPointClick")
        if (mapPoint.id == WAREHOUSE_ID) {
        } else {
            val indexItemClick = mapPoint.id.toInt()
            changeSelectedMapPoint(mapPoint)
            val isMapSelected = isMapSelected(indexItemClick)
            changeSelectedOrderItems(indexItemClick, isMapSelected)
//            //updateAndScrollToItems(indexItemClick)
//            changeShowOrders(isMapSelected)
            updateMarkers()
        }
    }

    private fun changeSelectedOrderItems(indexItemClick: Int, isMapSelected: Boolean) {
        orderItems.forEachIndexed { index, item ->
            if (item is CourierOrderItem) {
                item.isSelected =
                    if (index == indexItemClick) isMapSelected
                    else false
            }
        }
    }

    private fun isMapSelected(indexItemClick: Int) =
        mapMarkers[indexItemClick].icon == resourceProvider.getOrderMapSelectedIcon()

    private fun changeSelectedMapPoint(mapPoint: MapPoint) {
        mapMarkers.forEach { item ->
            if (item.point.id == WAREHOUSE_ID) return@forEach
            item.icon =
                if (item.point.id == mapPoint.id &&
                    item.icon == resourceProvider.getOrderMapIcon()
                )
                    resourceProvider.getOrderMapSelectedIcon()
                else resourceProvider.getOrderMapIcon()
        }
    }

    private fun observeMapActionError(throwable: Throwable) {
        onTechErrorLog("observeMapActionError", throwable)
    }

//    private fun observeNetworkState() {
//        addSubscription(
//            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
//        )
//    }

    private fun initToolbarLabel() {
        _toolbarLabelState.value = Label(parameters.address)
    }

//    private fun fetchVersionApp() {
//        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
//    }

    private fun lockState() {
        _holdState.value = true
    }

    private fun unlockState() {
        _holdState.value = false
    }

    private fun initOrders() {
        lockState()
        showProgress()
        addSubscription(interactor.orders(parameters.warehouseId)
            .doOnSuccess { orderEntities = it.toMutableList() }
            .doOnSuccess { saveOrderEntities(it) }
            .doOnSuccess { convertAndSaveItemsPointsMarkers(it) }
//            .flatMap { orderEntitiesToItem(it) }
            .subscribe({ ordersComplete() }, { ordersError(it) })
        )
    }

    private fun saveOrderEntities(warehouseEntities: List<CourierOrderEntity>) {
        this.orderEntities = warehouseEntities.toMutableList()
    }

    private fun convertAndSaveItemsPointsMarkers(orders: List<CourierOrderEntity>) {
//        onTechEventLog("courierWarehouseComplete", "warehouses count " + warehouses.size)

        val orderItems = mutableListOf<BaseItem>()
        val centerGroupPoints = mutableListOf<CoordinatePoint>()
        val mapMarkers = mutableListOf<CourierMapMarker>()

        val warehouseMapPoint =
            MapPoint(WAREHOUSE_ID, parameters.warehouseLatitude, parameters.warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        mapMarkers.add(warehouseMapMarker)

        orders.forEachIndexed { index, item ->

            val orderItem = dataBuilder.buildOrderItem(index, item, false)
            //CourierOrderItem CourierWarehouseItem(item.id, item.name, item.fullAddress, false)
            orderItems.add(orderItem)

            val coordinatePoints = mutableListOf<CoordinatePoint>()
            item.dstOffices.forEach { dstOffices ->
                val coordinatePoint = CoordinatePoint(dstOffices.lat, dstOffices.long)
                coordinatePoints.add(coordinatePoint)
            }
            val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints)
            val centerGroupPoint =
                CoordinatePoint(boundingBox.centerLatitude, boundingBox.centerLongitude)
            centerGroupPoints.add(centerGroupPoint)

            val mapPoint =
                MapPoint(index.toString(), centerGroupPoint.latitude, centerGroupPoint.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getOrderMapIcon())
            mapMarkers.add(mapMarker)
        }
        saveOrderItems(orderItems)
        saveCenterGroupPoints(centerGroupPoints)
        saveMapMarkers(mapMarkers)
    }

    private fun saveOrderItems(orderItems: List<BaseItem>) {
        this.orderItems = orderItems.toMutableList()
    }

    private fun saveCenterGroupPoints(mapMarkers: List<CoordinatePoint>) {
        this.centerGroupPoints = mapMarkers.toMutableList()
    }

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

//    private fun orderEntitiesToItem(orders: List<CourierOrderEntity>) =
//        Observable.fromIterable(orders.withIndex())
//            .map { (index, item): IndexedValue<CourierOrderEntity> ->
//                dataBuilder.buildOrderItem(index, item)
//            }
//            .toList()

    private fun showProgress() {
        _progressState.value = CourierOrdersProgressState.Progress
    }

    private fun ordersComplete() {
        if (orderItems.isEmpty()) {
            ordersEmpty()
            _orders.value = CourierOrderItemState.Empty(resourceProvider.getDialogEmpty())
        } else {
            updateMarkers()
            zoomAllGroupMarkersFromBoundingBox()
            showOrders()
        }

//        onTechEventLog(
//            "ordersComplete",
//            "courierOrderUIListState " + courierOrderUIListState.toString()
//        )

        _progressState.value = CourierOrdersProgressState.Complete
        unlockState()
    }

    private fun showOrders() {
        _orders.value = CourierOrderItemState.ShowOrders(orderItems)
    }


    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkersWithIndex(mapMarkers))
        // TODO: 24.01.2022 отобразить склад
//        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation))
    }

    private fun zoomAllGroupMarkersFromBoundingBox() {
        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(centerGroupPoints)
        interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
    }

    private fun ordersEmpty() {
        _navigateToDialogInfo.value = NavigateToDialogInfo(
            DialogInfoStyle.WARNING.ordinal,
            resourceProvider.getDialogTitle(),
            resourceProvider.getDialogMessage(),
            resourceProvider.getDialogButton()
        )
    }

    private fun ordersError(throwable: Throwable) {
        onTechErrorLog("ordersError", throwable)
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
        _orders.value = CourierOrderItemState.Empty(message.title)
        progressComplete()
        unlockState()
    }

    private fun progressComplete() {
        _progressState.value = CourierOrdersProgressState.Complete
    }

    fun onItemClick(idView: Int) {
        onTechEventLog("onItemClick")
        lockState()
        val courierOrderEntity = orderEntities[idView]
        addSubscription(
            interactor.clearAndSaveSelectedOrder(courierOrderEntity)
                .subscribe(
                    { clearAndSaveSelectedOrderComplete(idView) },
                    { clearAndSaveSelectedOrderError(it) })
        )
    }

    private fun clearAndSaveSelectedOrderComplete(idView: Int) {
        onTechEventLog("clearAndSaveSelectedOrderComplete")
        unlockState()
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(
                parameters.address,
                orderEntities[idView]
            )
    }

    private fun clearAndSaveSelectedOrderError(throwable: Throwable) {
        onTechErrorLog("ordersError", throwable)
        unlockState()
    }

    fun onUpdateClick() {
        initOrders()
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrders"
    }

    data class Label(val label: String)

}