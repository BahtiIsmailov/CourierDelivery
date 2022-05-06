package ru.wb.go.ui.courierorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import org.osmdroid.util.BoundingBox
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.CustomException
import ru.wb.go.network.exceptions.HttpObjectNotFoundException
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.ADDRESS_MAP_PREFIX
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.WAREHOUSE_ID
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
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
) : ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

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

    object VisibleShowAll

    private val _visibleShowAll = SingleLiveEvent<VisibleShowAll>()
    val visibleShowAll: LiveData<VisibleShowAll>
        get() = _visibleShowAll

    private var orderLocalDataEntities = mutableListOf<CourierOrderLocalDataEntity>()
    private var orderItems = mutableListOf<BaseItem>()

    private var orderMapMarkers = mutableListOf<CourierMapMarker>()
    private var orderCenterGroupPoints = mutableListOf<CoordinatePoint>()

    private var orderAddressItems = mutableListOf<CourierOrderDetailsAddressItem>()
    private var addressCoordinatePoints = mutableListOf<CoordinatePoint>()
    private var addressMapMarkers = mutableListOf<CourierMapMarker>()

    private var height = 0

    init {
        onTechEventLog("init")
    }

    fun init() {
        checkDemoMode()
        observeMapAction()
    }

    fun updateOrders(height: Int) {
        checkDemoMode()
        initOrders(height)
    }

    fun restoreDetails() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.freeOrdersLocal()
                .doOnSuccess { this.orderLocalDataEntities = it }
                .subscribe(
                    {
                        addressLabel()
                        convertAndSaveOrderPointMarkers(this.orderLocalDataEntities)
                        updateOrderAndWarehouseMarkers()
                        showAllAndOrderItems()
                        initOrderDetails(interactor.selectedRowOrder())
                        setLoader(WaitLoader.Complete)
                    },
                    { initOrdersError(it) })
        )
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

    fun onShowOrderDetailsClick() {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(interactor.isDemoMode())
    }

    fun onChangeCarNumberClick() {
        onTechEventLog("onChangeCarNumberClick")
        withSelectedRowOrder(navigateToEditCarNumber())
    }

    private fun withSelectedRowOrder(action: (rowOrder: Int) -> Unit) {
        action(interactor.selectedRowOrder())
    }

    private fun navigateToEditCarNumber(): (rowOrder: Int) -> Unit = {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToCarNumber(
                result = CourierCarNumberResult.Edit(it)
            )
    }

    fun toRegistrationClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToRegistration
    }

    fun onConfirmTakeOrderClick() {
        when {
            interactor.isDemoMode() -> navigateToRegistrationDialog()
            interactor.carNumberIsConfirm() -> withSelectedRowOrder(navigateToDialogConfirmScoreInfo())
            else -> withSelectedRowOrder(navigateToCreateCarNumber())
        }
    }

    private fun navigateToRegistrationDialog() {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToRegistrationDialog
    }

    private fun navigateToCreateCarNumber(): (rowOrder: Int) -> Unit = {
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToCarNumber(
                result = CourierCarNumberResult.Create(it)
            )
    }

    private fun navigateToDialogConfirmScoreInfo(): (rowOrder: Int) -> Unit = {
        with(orderLocalDataEntities[it]) {
            _navigateToDialogConfirmScoreInfo.value =
                NavigateToDialogConfirmInfo(
                    DialogInfoStyle.INFO.ordinal,
                    resourceProvider.getConfirmTitleDialog(courierOrderLocalEntity.id),
                    resourceProvider.getConfirmMessageDialog(
                        CarNumberUtils.numberFormat(interactor.carNumber()),
                        courierOrderLocalEntity.minVolume,
                        courierOrderLocalEntity.reservedDuration
                    ),
                    resourceProvider.getConfirmPositiveDialog(),
                    resourceProvider.getConfirmNegativeDialog()
                )
        }
    }

    private fun observeMapActionComplete(courierMapAction: CourierMapAction) {
        when (courierMapAction) {
            is CourierMapAction.ItemClick -> onMapPointClick(courierMapAction.point)
            is CourierMapAction.MapClick -> onMapClick()
            is CourierMapAction.ShowAll -> onShowAllClick()
            CourierMapAction.AnimateComplete -> {}
            is CourierMapAction.LocationUpdate -> {}
        }
    }

    private fun onShowAllClick() {
        _visibleShowAll.value = VisibleShowAll
    }

    private fun onMapClick() {
        _navigationState.value = CourierOrdersNavigationState.OnMapClick
    }

    fun onMapClickWithDetail() {
        _navigationState.value = CourierOrdersNavigationState.CloseAddressesDetail
        unselectedAddressMapMarkers()
        updateAddressMarkers()
        unselectedAddressItems()
        initAddressItems(orderAddressItems)
    }

    private fun unselectedAddressMapMarkers() {
        addressMapMarkers.forEach { it.icon = resourceProvider.getOfficeMapIcon() }
    }

    private fun unselectedAddressItems() {
        orderAddressItems.forEach { it.isSelected = false }
    }

    private fun onMapPointClick(mapPoint: MapPoint) {
        onTechEventLog("onItemPointClick", "mapPoint.id = " + mapPoint.id)
        if (mapPoint.id.startsWith(ADDRESS_MAP_PREFIX)) addressMapClick(mapPoint)
        else if (mapPoint.id != WAREHOUSE_ID) orderMapClick(mapPoint)
    }

    private fun orderMapClick(mapPoint: MapPoint) {
        val indexItem = mapPoint.id.toInt() - 1
        onOrderClick(indexItem)
    }

    private fun addressMapClick(mapPoint: MapPoint) {
        changeSelectedAddressMapPointAndItemByMap(mapPoint.id)
        updateAddressMarkers()
        updateShowingAddressDetail(getIdMapWithoutPrefix(mapPoint))
        initAddressItems(orderAddressItems)
    }

    private fun getIdMapWithoutPrefix(mapPoint: MapPoint) =
        mapPoint.id.replace(ADDRESS_MAP_PREFIX, "").toInt()

    private fun updateShowingAddressDetail(idMapClick: Int) {
        val address = orderAddressItems[idMapClick]
        _navigationState.value = if (address.isSelected)
            CourierOrdersNavigationState.ShowAddressDetail(address.fullAddress)
        else CourierOrdersNavigationState.CloseAddressesDetail
    }


    private fun updateAddressMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(addressMapMarkers))
    }

    private fun changeSelectedAddressMapPointAndItemByMap(mapPointId: String) {
        addressMapMarkers.forEachIndexed { index, item ->
            val addressItem = orderAddressItems[index]
            item.icon =
                if (item.point.id == mapPointId && item.icon == resourceProvider.getOfficeMapIcon()) {
                    addressItem.isSelected = true
                    resourceProvider.getOfficeMapSelectedIcon()
                } else {
                    addressItem.isSelected = false
                    resourceProvider.getOfficeMapIcon()
                }
        }
    }

    private fun observeMapActionError(throwable: Throwable) {
        onTechErrorLog("observeMapActionError", throwable)
    }

    private fun addressLabel() {
        _toolbarLabelState.value = Label(parameters.address)
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private fun initOrders(height: Int) {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.freeOrdersLocalClearAndSave(parameters.warehouseId)
                .doOnSuccess { this.orderLocalDataEntities = it }
                .subscribe(
                    { initOrdersComplete(height) },
                    { initOrdersError(it) })
        )
    }

    private fun initOrdersComplete(height: Int) {
        addressLabel()
        convertAndSaveOrderPointMarkers(this.orderLocalDataEntities)
        setLoader(WaitLoader.Complete)
        ordersComplete(height)
    }

    private fun initOrdersError(it: Throwable) {
        onTechErrorLog("ordersError", it)
        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
        _orderItems.value = CourierOrderItemState.Empty("Ошибка получения данных")
        setLoader(WaitLoader.Complete)
    }

    private fun convertAndSaveOrderAddressMapMarkersAndItems(dstOffices: List<CourierOrderDstOfficeLocalEntity>) {
        val addressItems = mutableListOf<CourierOrderDetailsAddressItem>()
        val addressCoordinatePoints = mutableListOf<CoordinatePoint>()
        val addressMapMarkers = mutableListOf<CourierMapMarker>()
        val warehouseLatitude = parameters.warehouseLatitude
        val warehouseLongitude = parameters.warehouseLongitude
        val warehouseMapPoint = MapPoint(WAREHOUSE_ID, warehouseLatitude, warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        addressMapMarkers.add(warehouseMapMarker)
        dstOffices.forEachIndexed { index, item ->
            addressItems.add(CourierOrderDetailsAddressItem(item.name, false))
            addressCoordinatePoints.add(CoordinatePoint(item.latitude, item.longitude))
            val mapPoint = MapPoint("$ADDRESS_MAP_PREFIX$index", item.latitude, item.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getOfficeMapIcon())
            addressMapMarkers.add(mapMarker)
        }
        saveAddressItems(addressItems)
        initAddressItems(addressItems)
        saveAddressCoordinatePoints(addressCoordinatePoints)
        saveAddressMapMarkers(addressMapMarkers)
    }

    private fun convertAndSaveOrderPointMarkers(orders: List<CourierOrderLocalDataEntity>) {
        val orderItems = mutableListOf<BaseItem>()
        val orderCenterGroupPoints = mutableListOf<CoordinatePoint>()
        val orderMapMarkers = mutableListOf<CourierMapMarker>()
        val warehouseLatitude = parameters.warehouseLatitude
        val warehouseLongitude = parameters.warehouseLongitude
        val warehouseMapPoint =
            MapPoint(WAREHOUSE_ID, warehouseLatitude, warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        orderMapMarkers.add(warehouseMapMarker)
        orders.forEachIndexed { index, item ->
            val idPoint = (index + 1).toString()
            val orderItem = dataBuilder.buildOrderItem(idPoint, index, item)
            orderItems.add(orderItem)
            val coordinatePoints = mutableListOf<CoordinatePoint>()
            item.dstOffices.forEach { dstOffices ->
                coordinatePoints.add(CoordinatePoint(dstOffices.latitude, dstOffices.longitude))
            }
            val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints)
            val centerGroupPoint =
                CoordinatePoint(boundingBox.centerLatitude, boundingBox.centerLongitude)
            orderCenterGroupPoints.add(centerGroupPoint)
            val mapPoint = MapPoint(idPoint, centerGroupPoint.latitude, centerGroupPoint.longitude)
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
            updateOrderAndWarehouseMarkers()
            zoomAllGroupMarkersFromBoundingBox(height)
            showAllAndOrderItems()
        }
    }

    private fun updateOrderAndWarehouseMarkers() {
        val warehouseMapMarker = mutableListOf(orderMapMarkers.first())
        val orders = orderMapMarkers.toMutableList().apply { removeFirst() }
        interactor.mapState(CourierMapState.UpdateMarkers(warehouseMapMarker))
        interactor.mapState(CourierMapState.UpdateMarkersWithIndex(orders))
    }

    private fun zoomAllGroupMarkersFromBoundingBox(height: Int) {
        interactor.mapState(
            CourierMapState.ZoomToBoundingBoxOffsetY(
                boundingBoxWithOrderCenterGroupWarehouseCoordinatePoint(),
                true,
                offsetY(height)
            )
        )
    }

    private fun offsetY(height: Int) = (height / 2) * -1

    private fun boundingBoxWithOrderCenterGroupWarehouseCoordinatePoint(): BoundingBox {
        orderCenterGroupPoints.add(warehouseCoordinatePoint())
        val boundingBox =
            MapEnclosingCircle().allCoordinatePointToBoundingBox(orderCenterGroupPoints)
        orderCenterGroupPoints.removeLast()
        return boundingBox
    }

    fun onShowAllOrdersClick(height: Int) {
        zoomAllGroupMarkersFromBoundingBox(height)
    }

    fun onShowAllOrderDetailsClick() {
        zoomAllOrderAddressPoints()
    }

    private fun showAllAndOrderItems() {
        interactor.mapState(CourierMapState.VisibleShowAll)
        _orderItems.value = CourierOrderItemState.ShowItems(orderItems)
    }

    fun onChangeCarNumberOrders(result: CourierCarNumberResult) {
        when (result) {
            is CourierCarNumberResult.Create -> {
                if (interactor.carNumberIsConfirm()) {
                    withSelectedRowOrder(navigateToDialogConfirmScoreInfo())
                }
            }
            is CourierCarNumberResult.Edit -> {}
        }
    }

    fun onOrderItemClick(clickItemIndex: Int) {
        onTechEventLog("onItemClick", "idView $clickItemIndex")
        onOrderClick(clickItemIndex)
    }

    private fun onOrderClick(itemIndex: Int) {
        onTechEventLog("onTakeOrderClick")
        interactor.saveRowOrder(itemIndex)
        initOrderDetails(itemIndex)
        _navigationState.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(interactor.isDemoMode())
    }

    fun onAddressesClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToAddresses
    }

    private fun warehouseCoordinatePoint() =
        CoordinatePoint(parameters.warehouseLatitude, parameters.warehouseLongitude)

    private fun initOrderDetails(itemIndex: Int) {
        with(orderLocalDataEntities[itemIndex]) {
            initOrderDetails(itemIndex, courierOrderLocalEntity, dstOffices.size)
            convertAndSaveOrderAddressMapMarkersAndItems(dstOffices)
            removeWarehouseFromAddressMapMarker()
        }
        val hideOrderMarkers = orderMapMarkers.toMutableList()
        hideOrderMarkers.removeFirst()
        interactor.mapState(
            CourierMapState.UpdateMarkersWithAnimateToPositions(
                pointsHide = hideOrderMarkers,
                pointFrom = hideOrderMarkers[itemIndex],
                pointsTo = addressMapMarkers,
                animateTo = addressesBoundingBox(),
                offsetY = DETAILS_HEIGHT
            )
        )
    }

    private fun removeWarehouseFromAddressMapMarker() {
        addressMapMarkers.removeAt(WAREHOUSE_FIRST_INDEX)
    }

    private fun zoomAllOrderAddressPoints() {
        interactor.mapState(
            CourierMapState.ZoomToBoundingBoxOffsetY(addressesBoundingBox(), true, DETAILS_HEIGHT)
        )
    }

    private fun addressesBoundingBox() =
        MapEnclosingCircle().allCoordinatePointToBoundingBox(addressCoordinatePoints)

    private fun initOrderDetails(
        idView: Int,
        courierOrderEntity: CourierOrderLocalEntity,
        pvz: Int
    ) {
        with(courierOrderEntity) {
            onTechEventLog("initOrderInfo", "order id: $id pvz: $pvz")
            val carNumber = carNumberFormat(interactor.carNumber())
            val itemId = (idView + 1).toString()
            val coast = DecimalFormat("#,###.##").format(minPrice)
            _orderDetails.value = CourierOrderDetailsInfoUIState.InitOrderDetails(
                carNumber = carNumber,
                isChangeCarNumber = interactor.carNumberIsConfirm(),
                itemId = itemId,
                orderId = resourceProvider.getOrder(id),
                cost = resourceProvider.getCost(coast),
                cargo = resourceProvider.getCargo(minVolume, minBoxesCount),
                countPvz = resourceProvider.getCountPvz(pvz),
                reserve = resourceProvider.getArrive(reservedDuration)
            )
        }
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

    fun onAddressItemClick(index: Int) {
        changeSelectedAddressMapPointAndItemByItemAddress(index)
        updateAddressMarkers()
        updateShowingAddressDetail(index)
        initAddressItems(orderAddressItems)
    }

    private fun changeSelectedAddressMapPointAndItemByItemAddress(index: Int) {
        val isSelected = !orderAddressItems[index].isSelected
        orderAddressItems.forEachIndexed { addressIndex, item ->
            val addressMapMarker = addressMapMarkers[addressIndex]
            if (index == addressIndex) {
                addressMapMarker.icon = if (isSelected)
                    resourceProvider.getOfficeMapSelectedIcon()
                else resourceProvider.getOfficeMapIcon()
                item.isSelected = isSelected
            } else {
                addressMapMarker.icon = resourceProvider.getOfficeMapIcon()
                item.isSelected = false
            }

        }
    }

    fun onCloseOrderDetailsClick(height: Int) {
        this.height = height
        _navigationState.value = CourierOrdersNavigationState.CloseAddressesDetail
        withSelectedRowOrder(makeOrderAddresses())
    }

    private fun makeOrderAddresses(): (rowOrder: Int) -> Unit = {
        _navigationState.value = CourierOrdersNavigationState.NavigateToOrders
        interactor.mapState(
            CourierMapState.UpdateMarkersWithAnimateToPosition(
                pointsShow = orderMapMarkers,
                pointsFrom = addressMapMarkers,
                pointTo = orderMapMarkerWithoutWarehouse(it),
                animateTo = boundingBoxWithOrderCenterGroupWarehouseCoordinatePoint(),
                offsetY = offsetY(height)
            )
        )
    }

    private fun orderMapMarkerWithoutWarehouse(itemIndex: Int) =
        orderMapMarkers[itemIndex + 1]

    fun onTaskNotExistConfirmClick() {
        _navigationState.value = CourierOrdersNavigationState.NavigateToWarehouse
    }

    fun onConfirmOrderClick() {
        onTechEventLog("onConfirmOrderClick")
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.anchorTask()
                .subscribe(
                    { anchorTaskComplete() },
                    { anchorTaskError(it) }
                )
        )
    }

    private fun anchorTaskComplete() {
        setLoader(WaitLoader.Complete)
        _navigationState.value = CourierOrdersNavigationState.NavigateToTimer
    }

    private fun anchorTaskError(it: Throwable) {
        onTechErrorLog("anchorTaskError", it)
        setLoader(WaitLoader.Complete)
        if (it is HttpObjectNotFoundException || it is BadRequestException) {
            val ex = CustomException(resourceProvider.getTaskReject())
            errorDialogManager.showErrorDialog(ex, _navigateToDialogInfo)
        } else {
            errorDialogManager.showErrorDialog(
                it,
                _navigateToDialogInfo,
                DialogInfoFragment.DIALOG_INFO2_TAG
            )
        }
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
        const val DETAILS_HEIGHT = -400
    }

    data class Label(val label: String)

}