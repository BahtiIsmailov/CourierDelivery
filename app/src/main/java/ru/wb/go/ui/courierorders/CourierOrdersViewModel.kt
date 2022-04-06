package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.exceptions.CustomException
import ru.wb.go.network.exceptions.HttpObjectNotFoundException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.WAREHOUSE_ID
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem
import ru.wb.go.ui.courierorders.domain.CourierOrdersInteractor
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
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

    private val _waitLoader = SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _demoState = MutableLiveData<Boolean>()
    val demoState: LiveData<Boolean>
        get() = _demoState

    private val _orderDetails = MutableLiveData<CourierOrderDetailsInfoUIState>()
    val orderDetails: LiveData<CourierOrderDetailsInfoUIState>
        get() = _orderDetails

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

    private var selectedOrderId: Int = -1

    init {
        onTechEventLog("init")
    }

    fun resumeInit() {
        checkDemoMode()
        observeMapAction()
    }

    fun update(height: Int) {
        checkDemoMode()
        initToolbarLabel()
        initOrders(height)
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

    fun onShowOrderDetailsClick() {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(interactor.isDemoMode())
    }

    fun onChangeCarNumberClick() {
        onTechEventLog("onChangeCarNumberClick")
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToCarNumber(
                result = CourierCarNumberResult.Edit(selectedOrderId)
            )
    }

    fun toRegistrationClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToRegistration
    }

    fun onConfirmTakeOrderClick() {
        when {
            interactor.isDemoMode() -> _navigationState.value =
                CourierOrdersNavigationState.NavigateToRegistrationDialog
            interactor.carNumberIsConfirm() -> navigateToDialogConfirmScoreInfo()
            else -> navigateToCreateCarNumber()
        }
    }

    private fun navigateToCreateCarNumber() {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToCarNumber(
                result = CourierCarNumberResult.Create(selectedOrderId)
            )
    }

    private fun navigateToDialogConfirmScoreInfo() {
        with(orderEntities[selectedOrderId]) {
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
            clearAndSaveSelectedOrder(indexItem)
        }
    }

    private fun changeSelectedOrderId(itemId: Int) {
        selectedOrderId = itemId
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
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.getFreeOrders(parameters.warehouseId)
                .subscribe({
                    orderEntities = it.sortedBy { o -> o.id }.toMutableList()
                    convertAndSaveOrderPointMarkers(orderEntities)
                    setLoader(WaitLoader.Complete)
                    ordersComplete(height)
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

    private fun ordersComplete(height: Int) {
        if (orderItems.isEmpty()) {
            _orderItems.value = CourierOrderItemState.Empty(resourceProvider.getDialogEmpty())
        } else {
            updateOrderMarkers()
            zoomAllGroupMarkersFromBoundingBox(height)
            showItems()
        }
    }

    private fun updateOrderMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkersWithIndex(orderMapMarkers))
    }

    private fun zoomAllGroupMarkersFromBoundingBox(height: Int) {
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

    fun onShowAllOrdersClick(height: Int) {
        zoomAllGroupMarkersFromBoundingBox(height)
    }

    fun onShowAllOrderDetailsClick() {
        zoomAllOrderAddressPoints()
    }

    private fun showItems() {
        _orderItems.value = CourierOrderItemState.ShowItems(orderItems)
    }

    fun onChangeCarNumberOrders(result: CourierCarNumberResult) {
        when (result) {
            is CourierCarNumberResult.Create ->
                if (interactor.carNumberIsConfirm()) {
                    updateOrderDetailsWithCarNumberChecked(result.id)
                    navigateToDialogConfirmScoreInfo()
                }
            is CourierCarNumberResult.Edit -> updateOrderDetailsWithCarNumberChecked(result.id)
        }
    }

    private fun updateOrderDetailsWithCarNumberChecked(clickItemIndex: Int) {
        val orderEntity = orderEntities[clickItemIndex]
        initOrderAddressMapMarkersAndItems(orderEntity.dstOffices)
        initOrderDetails(clickItemIndex, orderEntity, orderEntity.dstOffices.size)
        interactor.mapState(CourierMapState.UpdateMarkers(addressMapMarkers.toMutableList()))
        removeWarehouseMapMarker()
        zoomAllOrderAddressPoints()
    }

    fun onOrderClick(clickItemIndex: Int) {
        onTechEventLog("onItemClick", "idView $clickItemIndex")
        clearAndSaveSelectedOrder(clickItemIndex)
    }

    private fun clearAndSaveSelectedOrder(index: Int) {
        val courierOrderEntity = orderEntities[index]
        addSubscription(
            interactor.clearAndSaveSelectedOrder(courierOrderEntity)
                .subscribe(
                    { navigateToOrderDetails(index) },
                    { onTechErrorLog("ordersError", it) })
        )
    }

    fun onAddressesClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToAddresses
    }

    private fun warehouseCoordinatePoint() =
        CoordinatePoint(parameters.warehouseLatitude, parameters.warehouseLongitude)

    private fun navigateToOrderDetails(itemIndex: Int) {
        onTechEventLog("onTakeOrderClick")
        changeSelectedOrderId(itemIndex)
        initOrderDetails(itemIndex)
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(interactor.isDemoMode())
    }

    private fun initOrderDetails(idView: Int) {
        updateOrderMarkers()
        val orderEntity = orderEntities[idView]
        initOrderDetails(idView, orderEntity, orderEntity.dstOffices.size)
        interactor.mapState(CourierMapState.NavigateToPoint(orderCenterGroupPoints[idView]))
        initOrderAddressMapMarkersAndItems(orderEntity.dstOffices)
        removeWarehouseMapMarker()
        zoomAllOrderAddressPoints()

        interactor.mapState(
            CourierMapState.UpdateMarkersWithAnimateToPosition(
                orderMapMarkers,
                orderCenterGroupPoints[idView],
                addressMapMarkers
            )
        )
    }

    private fun removeWarehouseMapMarker() {
        addressMapMarkers.removeAt(WAREHOUSE_FIRST_INDEX)
    }

    private fun zoomAllOrderAddressPoints() {
        val boundingBox =
            MapEnclosingCircle().allCoordinatePointToBoundingBox(addressCoordinatePoints)
        interactor.mapState(
            CourierMapState.ZoomToBoundingBoxOffsetY(boundingBox, true, -400)
        )
    }

    private fun initOrderDetails(
        idView: Int,
        courierOrderEntity: CourierOrderEntity,
        pvz: Int
    ) {
        with(courierOrderEntity) {
            onTechEventLog("initOrderInfo", "order id: $id pvz: $pvz")
            val itemId = (orderItems[idView] as CourierOrderItem).lineNumber
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            val carNumber = carNumberFormat(interactor.carNumber())
            _orderDetails.value = CourierOrderDetailsInfoUIState.InitOrderDetails(
                carNumber = carNumber,
                isChangeCarNumber = interactor.carNumberIsConfirm(),
                itemId = itemId,
                orderId = resourceProvider.getOrder(id),
                cost = resourceProvider.getCost(coast),
                cargo = resourceProvider.getCargo(minBoxesCount, minVolume),
                countPvz = resourceProvider.getCountPvz(pvz),
                reserve = resourceProvider.getArrive(reservedDuration)
            )
        }
    }

    private fun initOrderAddressMapMarkersAndItems(dstOffices: List<CourierOrderDstOfficeEntity>) {
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

    private fun carNumberFormat(it: String) =
        it.let {
            if (it.isEmpty()) resourceProvider.getCarNumberEmpty()
            else resourceProvider.getCarNumber(CarNumberUtils.numberFormat(it))
        }

    fun onCloseOrdersClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToWarehouse
    }

    fun onCloseOrderDetailsClick(height: Int) {
        update(height)
        _navigationState.value = CourierOrdersNavigationState.NavigateToOrders
    }

    fun onTaskNotExistConfirmClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToWarehouse
    }

    fun onConfirmOrderClick() {
        onTechEventLog("onConfirmOrderClick")
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.anchorTask(orderEntities[selectedOrderId])
                .subscribe(
                    {
                        setLoader(WaitLoader.Complete)
                        _navigationState.value = CourierOrdersNavigationState.NavigateToTimer
                    },
                    {
                        onTechErrorLog("anchorTaskError", it)
                        setLoader(WaitLoader.Complete)
                        if (it is HttpObjectNotFoundException) {
                            val ex = CustomException("Заказ уже в работе. Выберите другой заказ.")
                            errorDialogManager.showErrorDialog(ex, _navigateToDialogInfo)
                        } else {
                            errorDialogManager.showErrorDialog(
                                it,
                                _navigateToDialogInfo,
                                DialogInfoFragment.DIALOG_INFO2_TAG
                            )
                        }
                    })
        )
    }

    fun goBack() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToWarehouse
    }

    fun onRegistrationConfirmClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToRegistration
    }

    fun onRegistrationCancelClick() {

    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrders"
        const val WAREHOUSE_FIRST_INDEX = 0
    }

    data class Label(val label: String)

}