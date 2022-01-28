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
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager
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
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _orderItems = MutableLiveData<CourierOrderItemState>()
    val orders: LiveData<CourierOrderItemState>
        get() = _orderItems

    private val _progressState = MutableLiveData<CourierOrdersProgressState>()
    val progressState: LiveData<CourierOrdersProgressState>
        get() = _progressState

    private val _navigationState = SingleLiveEvent<CourierOrdersNavigationState>()
    val navigationState: LiveData<CourierOrdersNavigationState>
        get() = _navigationState

    private val _holdState = MutableLiveData<Boolean>()
    val holdState: LiveData<Boolean>
        get() = _holdState

    private val _showDetailsState = MutableLiveData<CourierOrderShowDetailsState>()
    val showDetailsState: LiveData<CourierOrderShowDetailsState>
        get() = _showDetailsState

    private var orderEntities = mutableListOf<CourierOrderEntity>()
    private var orderItems = mutableListOf<BaseItem>()
    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var centerGroupPoints = mutableListOf<CoordinatePoint>()

    init {
        onTechEventLog("init")
        observeMapAction()
    }

    fun update() {
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

    private fun changeSelectedOrderItems(indexItemClick: Int, isMapSelected: Boolean) {
        orderItems.forEachIndexed { index, item ->
            if (item is CourierOrderItem) {
                item.isSelected =
                    if (index == indexItemClick) isMapSelected
                    else false
            }
        }
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
            .subscribe({ ordersComplete() }, { ordersError(it) })
        )
    }

    private fun saveOrderEntities(warehouseEntities: List<CourierOrderEntity>) {
        this.orderEntities = warehouseEntities.toMutableList()
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
            val idPoint = upToString(index)

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

    private fun showProgress() {
        _progressState.value = CourierOrdersProgressState.Progress
    }

    private fun ordersComplete() {
        if (orderItems.isEmpty()) {
            ordersEmpty()
            _orderItems.value = CourierOrderItemState.Empty(resourceProvider.getDialogEmpty())
        } else {
            updateMarkers()
            zoomAllGroupMarkersFromBoundingBox()
            showOrders()
        }

        hideProgress()
        unlockState()
    }

    private fun hideProgress() {
        _progressState.value = CourierOrdersProgressState.Complete
    }

    private fun showOrders() {
        _orderItems.value = CourierOrderItemState.ShowOrders(orderItems)
    }


    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkersWithIndex(mapMarkers))
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
        _orderItems.value = CourierOrderItemState.Empty(message.title)
        progressComplete()
        unlockState()
    }

    private fun progressComplete() {
        _progressState.value = CourierOrdersProgressState.Complete
    }

    fun onItemClick(idView: Int) {
        onTechEventLog("onItemClick", "idView $idView")
        changeItemSelected(idView)
    }

    private fun changeItemSelected(clickItemIndex: Int) {
        val isSelected = isInvertOrdersItemsSelected(clickItemIndex)
        changeSelectedMapPoint(mapMarkers[clickItemIndex + 1].point)
//        changeMapMarkers(clickItemIndex, isSelected)
        changeSelectedOrderItems(clickItemIndex, isSelected)
        updateItems()
        changeShowOrders(isSelected)
        updateMarkers()
    }

    private fun updateItems() {
        _orderItems.value = CourierOrderItemState.UpdateItems(orderItems)
    }


    private fun isInvertOrdersItemsSelected(selectIndex: Int) =
        !(orderItems[selectIndex] as CourierOrderItem).isSelected

    fun onDetailClick() {
        lockState()
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
                    { clearAndSaveSelectedOrderComplete(index) },
                    { clearAndSaveSelectedOrderError(it) })
        )
    }

    private fun clearAndSaveSelectedOrderComplete(idView: Int) {
        onTechEventLog("clearAndSaveSelectedOrderComplete")
        unlockState()
        checkCarNumberAndNavigate(idView)
    }

    private fun checkCarNumberAndNavigate(idView: Int) {
        onTechEventLog("onTakeOrderClick")
        val title = parameters.address
        val orderNumber = upToString(idView)
        val order = orderEntities[idView]
        _navigationState.value = if (interactor.carNumberIsConfirm())
            CourierOrdersNavigationState.NavigateToOrderDetails(
                title,
                orderNumber,
                order,
                parameters.warehouseLatitude,
                parameters.warehouseLongitude
            )
        else CourierOrdersNavigationState.NavigateToCarNumber(
            title,
            orderNumber,
            order,
            parameters.warehouseLatitude,
            parameters.warehouseLongitude
        )
    }

    private fun upToString(number: Int) = (number + 1).toString()

    private fun clearAndSaveSelectedOrderError(throwable: Throwable) {
        onTechErrorLog("ordersError", throwable)
        unlockState()
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