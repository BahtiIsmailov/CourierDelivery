package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.WAREHOUSE_ID
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsInfoUIState
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem
import ru.wb.go.ui.courierorders.domain.CourierOrdersInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.formatter.CarNumberUtils
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import java.text.DecimalFormat

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

    private val _orderInfo = MutableLiveData<CourierOrderDetailsInfoUIState>()
    val orderInfo: LiveData<CourierOrderDetailsInfoUIState>
        get() = _orderInfo

    private val _orderAddresses = MutableLiveData<CourierOrderAddressesUIState>()
    val orderAddresses: LiveData<CourierOrderAddressesUIState>
        get() = _orderAddresses

    private val _navigateToDialogConfirmScoreInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmScoreInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmScoreInfo

    private var orderEntities = mutableListOf<CourierOrderEntity>()
    private var orderItems = mutableListOf<BaseItem>()
    private var orderMapMarkers = mutableListOf<CourierMapMarker>()
    private var orderCenterGroupPoints = mutableListOf<CoordinatePoint>()

    private var orderAddressItems = mutableListOf<CourierOrderDetailsAddressItem>()
    private var addressCoordinatePoints = mutableListOf<CoordinatePoint>()
    private var addressMapMarkers = mutableListOf<CourierMapMarker>()

    private var selOrderId: Int = -1

    init {
        onTechEventLog("init")
        checkDemoMode()
    }

    fun update(height: Int) {

        LogUtils { logDebugApp("height orders " + height) }

        checkDemoMode()
        observeMapAction()
        initToolbarLabel()
        initOrders(height)

    }

    var height: Int = 0

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

    fun onChangeCarNumberClick() {
        onTechEventLog("onChangeCarNumberClick")
//        with(parameters) {
//            _navigationState.value = CourierOrdersNavigationState.NavigateToCarNumber(
//                title, orderNumber, order, warehouseLatitude, warehouseLongitude
//            )
//        }
    }

    fun toRegistrationClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToRegistration
    }

    fun confirmTakeOrderClick() {
        if (interactor.isDemoMode())
            _navigationState.value = CourierOrdersNavigationState.NavigateToRegistrationDialog
        else

        //val courierOrderEntity =

            with(orderEntities[selOrderId]) {
                _navigateToDialogConfirmScoreInfo.value =
                    NavigateToDialogConfirmInfo(
                        DialogInfoStyle.INFO.ordinal,
                        resourceProvider.getConfirmTitleDialog(id),
                        resourceProvider.getConfirmMessageDialog(
                            CarNumberUtils.numberFormat(interactor.carNumber()),
                            minVolume,
                            reservedDuration
                        ),
                        resourceProvider.getConfirmPositiveDialog(),
                        resourceProvider.getConfirmNegativeDialog()
                    )
            }
//
//        addSubscription(
//            interactor.observeOrderData()
//                .map { it.courierOrderLocalEntity }
//                .subscribe(
//                    {
//                        _navigateToDialogConfirmScoreInfo.value =
//                            NavigateToDialogConfirmInfo(
//                                DialogInfoStyle.INFO.ordinal,
//                                resourceProvider.getConfirmTitleDialog(it.id),
//                                resourceProvider.getConfirmMessageDialog(
//                                    CarNumberUtils.numberFormat(interactor.carNumber()),
//                                    it.minVolume,
//                                    it.reservedDuration
//                                ),
//                                resourceProvider.getConfirmPositiveDialog(),
//                                resourceProvider.getConfirmNegativeDialog()
//                            )
//                    },
//                    {
//
//                    })
//        )


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
        selOrderId = itemId
//        if (selOrderId != -1 && selOrderId != itemId) {
//            (orderItems[selOrderId] as CourierOrderItem).isSelected = false
//        }
        (orderItems[itemId] as CourierOrderItem).isSelected = isSelected
        //selOrderId = if (isSelected) itemId else -1
    }

    private fun isMapSelected(idItemClick: String): Boolean {
        var isSelected = false
        orderMapMarkers.forEach { item ->
            if (item.point.id == idItemClick && item.icon == resourceProvider.getOrderMapSelectedIcon()) {
                isSelected = true
                return@forEach
            }
        }
        return isSelected
    }

    private fun changeSelectedMapPoint(mapPoint: MapPoint) {
        orderMapMarkers.forEach { item ->
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

    private fun initOrders(height: Int) {
        this.height = height
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.getFreeOrders(parameters.warehouseId)
                .subscribe({
                    orderEntities = it.sortedBy { o -> o.id }.toMutableList()
                    selOrderId = -1
                    convertAndSaveOrderPointMarkers(orderEntities)
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


    private fun convertAndSaveOrderPointMarkers(orders: List<CourierOrderEntity>) {
        val orderItems = mutableListOf<BaseItem>()
        val orderCenterGroupPoints = mutableListOf<CoordinatePoint>()
        val orderMapMarkers = mutableListOf<CourierMapMarker>()

        val warehouseMapPoint =
            MapPoint(WAREHOUSE_ID, parameters.warehouseLatitude, parameters.warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        orderMapMarkers.add(warehouseMapMarker)

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
            orderCenterGroupPoints.add(centerGroupPoint)

            val mapPoint =
                MapPoint(idPoint, centerGroupPoint.latitude, centerGroupPoint.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getOrderMapIcon())
            orderMapMarkers.add(mapMarker)
        }
        this.orderItems = orderItems.toMutableList()
        this.orderCenterGroupPoints = orderCenterGroupPoints.toMutableList()
        this.orderMapMarkers = orderMapMarkers.toMutableList()
    }

    private fun ordersComplete() {
        if (orderItems.isEmpty()) {
            _orderItems.value = CourierOrderItemState.Empty(resourceProvider.getDialogEmpty())
        } else {
            updateMarkers()
            zoomAllGroupMarkersFromBoundingBox()
            showItems()
        }
    }

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkersWithIndex(orderMapMarkers))
    }

    private fun zoomAllGroupMarkersFromBoundingBox() {
        orderCenterGroupPoints.add(warehouseCoordinatePoint())
        val boundingBox =
            MapEnclosingCircle().allCoordinatePointToBoundingBox(orderCenterGroupPoints)
        interactor.mapState(
            CourierMapState.ZoomToBoundingBoxOffsetY(
                boundingBox,
                true,
                (height / 2) * -1
            )
        )
    }


    private fun showItems() {
        _orderItems.value = CourierOrderItemState.ShowItems(orderItems)
    }

    fun onItemClick(clickItemIndex: Int) {
        onTechEventLog("onItemClick", "idView $clickItemIndex")
//        val isSelected = !(orderItems[clickItemIndex] as CourierOrderItem).isSelected
//        changeSelectedMapPoint(orderMapMarkers[clickItemIndex + 1].point)
//        changeSelectedOrderItems(clickItemIndex, isSelected)
//        updateItems()
//        updateMarkers()
//        if (isSelected) zoomAllGroupMarkersFromBoundingBox(clickItemIndex)
//        else zoomAllGroupMarkersFromBoundingBox()
//        changeShowOrders(isSelected)

        checkAndNavigateToOrders(clickItemIndex)
    }

    private fun zoomAllGroupMarkersFromBoundingBox(index: Int) {
        val selectedGroup = mutableListOf(orderCenterGroupPoints[index])
        selectedGroup.add(warehouseCoordinatePoint())
        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(selectedGroup)
        interactor.mapState(
            CourierMapState.ZoomToBoundingBoxOffsetY(
                boundingBox,
                true,
                (height / 2) * -1
            )
        )
    }

//    fun onHeightInfoBottom(height: Int) {
//        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints)
//        interactor.mapState(
//            CourierMapState.ZoomToBoundingBoxOffsetY(
//                boundingBox,
//                true,
//                (height / 2) * -1
//            )
//        )
//    }

    private fun warehouseCoordinatePoint() =
        CoordinatePoint(parameters.warehouseLatitude, parameters.warehouseLongitude)

    private fun updateItems() {
        _orderItems.value = CourierOrderItemState.UpdateItems(orderItems)
    }

    fun onDetailClick() {
//        _showDetailsState.value = CourierOrderShowDetailsState.Disable
//        orderItems.forEachIndexed { index, items ->
//            if (items is CourierOrderItem) {
//                if (items.isSelected) {
//                    checkAndNavigateToOrders(index)
//                    return@forEachIndexed
//                }
//            }
//        }
    }

    private fun checkAndNavigateToOrders(index: Int) {
        //       val courierOrderEntity = orderEntities[index]
//        addSubscription(
//            interactor.clearAndSaveSelectedOrder(courierOrderEntity)
//                .subscribe(
//                    { checkCarNumberAndNavigate(index) },
//                    { onTechErrorLog("ordersError", it) })
//        )

        checkCarNumberAndNavigate(index)
    }

    private fun checkCarNumberAndNavigate(idView: Int) {
        clearSubscription()
        onTechEventLog("onTakeOrderClick")
        val title = parameters.address
        val itemId = (orderItems[idView] as CourierOrderItem).lineNumber
        val order = orderEntities[idView]
        _navigationState.value =
            if (interactor.carNumberIsConfirm() || interactor.isDemoMode()) {
//            CourierOrdersNavigationState.NavigateToOrderDetails(
//                title,
//                orderId,
//                order,
//                parameters.warehouseLatitude,
//                parameters.warehouseLongitude
//            )
                initOrderDetails(idView)
                CourierOrdersNavigationState.NavigateToOrderDetails
            } else CourierOrdersNavigationState.NavigateToCarNumber(
                title,
                itemId,
                order,
                parameters.warehouseLatitude,
                parameters.warehouseLongitude
            )
    }

    private fun initOrderDetails(idView: Int) {

        val orderEntity = orderEntities[idView]
        initOrderInfo(idView, orderEntity, orderEntity.dstOffices.size)
        interactor.mapState(CourierMapState.NavigateToPoint(orderCenterGroupPoints[idView]))
        initOrderAddressesItems(orderEntity.dstOffices)
        addressMapMarkers.removeAt(0)
        interactor.mapState(
            CourierMapState.UpdateMarkersWithAnimatePosition(
                orderCenterGroupPoints[idView],
                addressMapMarkers
            )
        )

//        addSubscription(
//            interactor.observeOrderData()
//                .subscribe(
//                    { observeOrderDataComplete(idView, it) },
//                    { observeOrderDataError(it) })
//        )
    }

//    private fun observeOrderDataComplete(idView: Int, it: CourierOrderLocalDataEntity) {
//        initOrderInfo(idView, it.courierOrderLocalEntity, it.dstOffices.size)
//        initOrderAddressesItems(it.dstOffices)
//    }

    private fun initOrderInfo(
        idView: Int,
        courierOrderEntity: CourierOrderEntity,
        pvz: Int
    ) {
        with(courierOrderEntity) {
            onTechEventLog("initOrderInfo", "order id: $id pvz: $pvz")
            val itemId = (orderItems[idView] as CourierOrderItem).lineNumber
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderDetailsInfoUIState.InitOrderInfo(
                itemId = itemId,
                orderId = resourceProvider.getOrder(id),
                cost = resourceProvider.getCost(coast),
                cargo = resourceProvider.getCargo(minBoxesCount, minVolume),
                countPvz = resourceProvider.getCountPvz(pvz),
                reserve = resourceProvider.getArrive(reservedDuration)
            )
            _orderInfo.value = carNumberFormat(interactor.carNumber())
        }
    }

    private fun initOrderAddressesItems(dstOffices: List<CourierOrderDstOfficeEntity>) {
        val addressItems = mutableListOf<CourierOrderDetailsAddressItem>()
        val addressCoordinatePoints = mutableListOf<CoordinatePoint>()
        val addressMapMarkers = mutableListOf<CourierMapMarker>()

        val warehouseLatitude = parameters.warehouseLatitude
        val warehouseLongitude = parameters.warehouseLongitude

        val warehouseMapPoint =
            MapPoint(WAREHOUSE_ID, warehouseLatitude, warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        addressMapMarkers.add(warehouseMapMarker)
        //addressCoordinatePoints.add(CoordinatePoint(warehouseLatitude, warehouseLongitude))

        dstOffices.forEachIndexed { index, item ->
            addressItems.add(CourierOrderDetailsAddressItem(item.name, false))
            addressCoordinatePoints.add(CoordinatePoint(item.lat, item.long))
            val mapPoint = MapPoint(index.toString(), item.lat, item.long)
            val mapMarker = Empty(mapPoint, resourceProvider.getOfficeMapIcon())
            addressMapMarkers.add(mapMarker)
        }
        saveAddressItems(addressItems)
        initAddressItems(addressItems)
        saveAddressCoordinatePoints(addressCoordinatePoints)
        saveAddressMapMarkers(addressMapMarkers)
        //interactor.mapState(CourierMapState.UpdateMarkers(addressMapMarkers))
//        val boundingBox =
//            MapEnclosingCircle().allCoordinatePointToBoundingBox(addressCoordinatePoints)
//        interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
    }

    private fun saveAddressItems(items: List<CourierOrderDetailsAddressItem>) {
        orderAddressItems = items.toMutableList()
    }

    private fun saveAddressCoordinatePoints(coordinatePoints: List<CoordinatePoint>) {
        this.addressCoordinatePoints = coordinatePoints.toMutableList()
    }

    private fun saveAddressMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.addressMapMarkers = mapMarkers.toMutableList()
    }

    private fun initAddressItems(items: MutableList<CourierOrderDetailsAddressItem>) {
        _orderAddresses.value = if (items.isEmpty()) CourierOrderAddressesUIState.Empty
        else CourierOrderAddressesUIState.InitItems(items)
    }

//    private fun observeOrderDataError(throwable: Throwable) {
//        onTechErrorLog("onUpdate", throwable)
//    }

    private fun carNumberFormat(it: String) =
        CourierOrderDetailsInfoUIState.NumberSpanFormat(it.let {
            if (it.isEmpty()) resourceProvider.getCarNumberEmpty()
            else resourceProvider.getCarNumber(CarNumberUtils.numberFormat(it))
        })

    fun onCancelLoadClick() {
        clearSubscription()
    }

    fun onCloseOrdersClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToWarehouse
    }

    fun onCloseOrderDetailsClick() {
        update(1200)
        _navigationState.value = CourierOrdersNavigationState.NavigateToOrders
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrders"
    }

    data class Label(val label: String)

}