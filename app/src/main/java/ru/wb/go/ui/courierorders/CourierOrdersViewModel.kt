package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.WAREHOUSE_ID
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem
import ru.wb.go.ui.courierorders.domain.CourierOrdersInteractor
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint

class CourierOrdersViewModel(
    private val parameters: CourierOrderParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrdersInteractor,
    private val dataBuilder: CourierOrdersDataBuilder,
    private val resourceProvider: CourierOrdersResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _orderItems = MutableLiveData<CourierOrderItemState>()
    val orders: LiveData<CourierOrderItemState>
        get() = _orderItems

    private val _navigationState = SingleLiveEvent<CourierOrdersNavigationState>()
    val navigationState: LiveData<CourierOrdersNavigationState>
        get() = _navigationState

    private val _showDetailsState = MutableLiveData<CourierOrderShowDetailsState>()
    val showDetailsState: LiveData<CourierOrderShowDetailsState>
        get() = _showDetailsState

    private val _waitLoader = SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _demoState = MutableLiveData<Boolean>()
    val demoState: LiveData<Boolean>
        get() = _demoState

    private var orderEntities = mutableListOf<CourierOrderEntity>()
    private var orderItems = mutableListOf<BaseItem>()
    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var centerGroupPoints = mutableListOf<CoordinatePoint>()

    private var selOrderId: Int = -1

    init {
        onTechEventLog("init")
        checkDemoMode()
    }

    fun update() {
        checkDemoMode()
        observeMapAction()
        initToolbarLabel()
        initOrders()
    }

    private fun checkDemoMode() {
        _demoState.value = interactor.isDemoMode()
    }

    private fun observeMapAction() {
        addSubscription(
            interactor.observeMapAction()
                .filter { it is CourierMapAction.ItemClick }
                .map { (it as CourierMapAction.ItemClick).point }
                .subscribe(
                    { observeMapActionComplete(it) },
                    { observeMapActionError(it) }
                ))
    }

    fun toRegistrationClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToRegistration
    }

    private fun observeMapActionComplete(it: MapPoint) {
        onMapPointClick(it)
    }

    private fun onMapPointClick(mapPoint: MapPoint) {
        onTechEventLog("onItemPointClick", "mapPoint.id = " + mapPoint.id)
        if (mapPoint.id != WAREHOUSE_ID) {
            val idMapClick = mapPoint.id
            val indexItem = idMapClick.toInt() - 1
            changeSelectedMapPoint(mapPoint)
            val isMapSelected = isMapSelected(idMapClick)
            changeSelectedOrderItems(indexItem, isMapSelected)
            updateAndScrollToItems(indexItem)
            changeShowOrders(isMapSelected)
            updateMarkers()
        }
    }

    private fun updateAndScrollToItems(indexItemClick: Int) {
        _orderItems.value = CourierOrderItemState.UpdateItems(orderItems)
        _orderItems.value = CourierOrderItemState.ScrollTo(indexItemClick)
    }

    private fun changeShowOrders(isSelected: Boolean) {
        _showDetailsState.value =
            if (isSelected) CourierOrderShowDetailsState.Enable else CourierOrderShowDetailsState.Disable
    }

    private fun changeSelectedOrderItems(itemId: Int, isSelected: Boolean) {
        if (selOrderId != -1 && selOrderId != itemId) {
            (orderItems[selOrderId] as CourierOrderItem).isSelected = false
        }
        (orderItems[itemId] as CourierOrderItem).isSelected = isSelected
        selOrderId = if (isSelected) itemId else -1
    }

    private fun isMapSelected(idItemClick: String): Boolean {
        var isSelected = false
        mapMarkers.forEach { item ->
            if (item.point.id == idItemClick && item.icon == resourceProvider.getOrderMapSelectedIcon()) {
                isSelected = true
                return@forEach
            }
        }
        return isSelected
    }

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

    private fun initToolbarLabel() {
        _toolbarLabelState.value = Label(parameters.address)
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private fun initOrders() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.getFreeOrders(parameters.warehouseId)
                .subscribe({
                    orderEntities = it.sortedBy { o -> o.id }.toMutableList()
                    selOrderId = -1
                    convertAndSaveItemsPointsMarkers(orderEntities)
                    setLoader(WaitLoader.Complete)
                    ordersComplete()
                }, {
                    onTechErrorLog("ordersError", it)

                    errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                    _orderItems.value = CourierOrderItemState.Empty("Ошибка получения данных")
                    setLoader(WaitLoader.Complete)
                })
        )
    }


    private fun convertAndSaveItemsPointsMarkers(orders: List<CourierOrderEntity>) {
        val orderItems = mutableListOf<BaseItem>()
        val centerGroupPoints = mutableListOf<CoordinatePoint>()
        val mapMarkers = mutableListOf<CourierMapMarker>()

        val warehouseMapPoint =
            MapPoint(WAREHOUSE_ID, parameters.warehouseLatitude, parameters.warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        mapMarkers.add(warehouseMapMarker)

        orders.forEachIndexed { index, item ->
            val idPoint = (index + 1).toString()

            val orderItem = dataBuilder.buildOrderItem(idPoint, index, item, false)
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
                MapPoint(idPoint, centerGroupPoint.latitude, centerGroupPoint.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getOrderMapIcon())
            mapMarkers.add(mapMarker)
        }
        this.orderItems = orderItems.toMutableList()
        this.centerGroupPoints = centerGroupPoints.toMutableList()
        this.mapMarkers = mapMarkers.toMutableList()
    }

    private fun ordersComplete() {
        if (orderItems.isEmpty()) {
            _orderItems.value = CourierOrderItemState.Empty(resourceProvider.getDialogEmpty())
        } else {
            updateMarkers()
            zoomAllGroupMarkersFromBoundingBox()
            _orderItems.value = CourierOrderItemState.ShowOrders(orderItems)
        }
    }

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkersWithIndex(mapMarkers))
    }

    private fun zoomAllGroupMarkersFromBoundingBox() {
        centerGroupPoints.add(
            CoordinatePoint(
                parameters.warehouseLatitude,
                parameters.warehouseLongitude
            )
        )
        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(centerGroupPoints)
        interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
    }

    fun onItemClick(clickItemIndex: Int) {
        onTechEventLog("onItemClick", "idView $clickItemIndex")
        val isSelected = !(orderItems[clickItemIndex] as CourierOrderItem).isSelected

        changeSelectedMapPoint(mapMarkers[clickItemIndex + 1].point)
        changeSelectedOrderItems(clickItemIndex, isSelected)
        _orderItems.value = CourierOrderItemState.UpdateItems(orderItems)
        changeShowOrders(isSelected)
        updateMarkers()
    }

    fun onDetailClick() {
        _showDetailsState.value = CourierOrderShowDetailsState.Disable
        orderItems.forEachIndexed { index, items ->
            if (items is CourierOrderItem) {
                if (items.isSelected) {
                    checkAndNavigateToOrders(index)
                    return@forEachIndexed
                }
            }
        }
    }

    private fun checkAndNavigateToOrders(index: Int) {
        val courierOrderEntity = orderEntities[index]
        addSubscription(
            interactor.clearAndSaveSelectedOrder(courierOrderEntity)
                .subscribe(
                    { checkCarNumberAndNavigate(index) },
                    { onTechErrorLog("ordersError", it) })
        )
    }

    private fun checkCarNumberAndNavigate(idView: Int) {
        clearSubscription()
        onTechEventLog("onTakeOrderClick")
        val title = parameters.address
        val orderId = (orderItems[idView] as CourierOrderItem).orderId
        val order = orderEntities[idView]
        _navigationState.value = if (interactor.carNumberIsConfirm() || interactor.isDemoMode())
            CourierOrdersNavigationState.NavigateToOrderDetails(
                title,
                orderId,
                order,
                parameters.warehouseLatitude,
                parameters.warehouseLongitude
            )
        else CourierOrdersNavigationState.NavigateToCarNumber(
            title,
            orderId,
            order,
            parameters.warehouseLatitude,
            parameters.warehouseLongitude
        )
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