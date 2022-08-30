package ru.wb.go.ui.courierwarehouses

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import retrofit2.HttpException
import ru.wb.go.app.AppPreffsKeys.CLOSE_FRAGMENT_WHEN_ENDED_TIME
import ru.wb.go.app.AppPreffsKeys.LOCATION_KEY
import ru.wb.go.app.AppPreffsKeys.SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP
import ru.wb.go.app.COURIER_ONLY_ONE_TASK_ERROR
import ru.wb.go.app.COURIER_ONLY_ONE_TASK_ERROR_400
import ru.wb.go.app.COURIER_TASK_ALREADY_RESERVED_ERROR
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.remote.courier.CourierWarehousesResponse
import ru.wb.go.network.exceptions.*
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.couriermap.CourierMapFragment.Companion.MY_LOCATION_ID
import ru.wb.go.ui.courierorders.*
import ru.wb.go.ui.courierwarehouses.items.CourierOrderItem
import ru.wb.go.ui.courierwarehouses.domain.CourierWarehousesInteractor
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.WaitLoaderForOrder
import ru.wb.go.utils.formatter.CarNumberUtils
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import ru.wb.go.utils.map.PointType
import ru.wb.go.utils.prefs.SharedWorker
import java.net.UnknownHostException
import kotlin.math.roundToInt

class CourierWarehousesViewModel(
    private val interactor: CourierWarehousesInteractor, 
    private val resourceProvider: CourierWarehousesResourceProvider,
    private val dataBuilder: CourierOrdersDataBuilder, 
    private val errorDialogManager: ErrorDialogManager,
    private val sharedWorker: SharedWorker,
) : ServicesViewModel(interactor, resourceProvider) {

    private val _warehouseState = SingleLiveEvent<CourierWarehouseItemState>()
    val warehouseState: LiveData<CourierWarehouseItemState>
        get() = _warehouseState

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _orderDetails = MutableLiveData<CourierOrderDetailsInfoUIState>()
    val orderDetails: LiveData<CourierOrderDetailsInfoUIState>
        get() = _orderDetails

    private val _navigateToDialogConfirmScoreInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmScoreInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmScoreInfo

    private val _navigationState = SingleLiveEvent<CourierWarehousesNavigationState>()
    val navigationState: LiveData<CourierWarehousesNavigationState>
        get() = _navigationState

    private val _navigationStateOrder = SingleLiveEvent<CourierOrdersNavigationState>()
    val navigationStateOrder: LiveData<CourierOrdersNavigationState>
        get() = _navigationStateOrder

    private val _visibleButtonBackLiveData = MutableLiveData<Boolean>()
    val visibleButtonBackLiveData: LiveData<Boolean> = _visibleButtonBackLiveData

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _waitLoaderForOrder =
        SingleLiveEvent<WaitLoaderForOrder>()
    val waitLoaderForOrder: LiveData<WaitLoaderForOrder>
        get() = _waitLoaderForOrder

    private val _showOrdersState = MutableLiveData<CourierWarehousesShowOrdersState>()
    val showOrdersState: LiveData<CourierWarehousesShowOrdersState>
        get() = _showOrdersState

    private val _demoState = MutableLiveData<Boolean>()
    val demoState: LiveData<Boolean>
        get() = _demoState

    private val _selectedMapPointForFragment = Channel<MapPoint>()
    val selectedMapPointForFragment = _selectedMapPointForFragment.receiveAsFlow()

    var selectedMapPointForFragment1: MapPoint? = null

    private var warehouseEntities = mutableSetOf<CourierWarehouseLocalEntity>()
    private var warehouseItems = mutableSetOf<CourierWarehouseItem>()
    private var mapMarkers = mutableSetOf<CourierMapMarker>()
    private var coordinatePoints = mutableListOf<CoordinatePoint>()
    private var oldEntity: CourierWarehouseLocalEntity? = null


    private var orderItems = mutableSetOf<BaseItem>()
    private var orderLocalDataEntities = listOf<CourierOrderLocalDataEntity>()

    private var orderMapMarkers = mutableSetOf<CourierMapMarker>()
    private var orderCenterGroupPoints = mutableSetOf<CoordinatePoint>()

    private var orderAddressItems = mutableSetOf<CourierOrderDetailsAddressItem>()
    private var addressCoordinatePoints = mutableListOf<CoordinatePoint>()
    private var addressMapMarkers = mutableListOf<CourierMapMarker>()

    private var myLocation: CoordinatePoint? = null
    private var mapPointAfterCarNumber: MapPoint? = null

    private var height = 0

    private var whSelectedId: Int? = null
    private var screenHeight: Int? = null


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
        val myLocationString = sharedWorker.load(LOCATION_KEY, "")
        if (myLocationString != "") {
            myLocation = CoordinatePoint(
                myLocationString.split(":")[0].toDouble(),
                myLocationString.split(":")[1].toDouble()
            )
        }
    }

    private fun observeMapAction() {
        interactor.observeMapAction()
            .onEach {
                when (it) {
                    is CourierMapAction.ItemClick -> {
                        if (!it.point.id.contains(MY_LOCATION_ID)) {
                            onMapPointClick(it.point)
                            _selectedMapPointForFragment.trySend(it.point)
                        }
                    }
                    is CourierMapAction.LocationUpdate -> {
                        initMapByLocation(it.point)
                    }
                    CourierMapAction.MapClick -> {
                        showManagerBar()
                    }
                    CourierMapAction.ShowAll -> {
                        onShowAllClick()
                    }
                    else -> {}
                }
            }
            .catch {
                logException(it, "observeMapAction")
            }
            .launchIn(viewModelScope)
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

    fun updateData() {
        clearMap()
        viewModelScope.launch {
            delay(50)
            when {
                navigationStateOrder.value is CourierOrdersNavigationState.NavigateToCarNumber -> {
                     initOrdersComplete(screenHeight!!)
                    _visibleButtonBackLiveData.value = true
                    _navigationStateOrder.value = CourierOrdersNavigationState.NavigateToWarehouse
                }
                navigationStateOrder.value is CourierOrdersNavigationState.CloseAddressesDetail -> {
                     initOrdersComplete(screenHeight!!)
                    _navigationStateOrder.value = CourierOrdersNavigationState.NavigateToWarehouse
                }
                navigationState.value is CourierWarehousesNavigationState.NavigateToCourierOrders -> {
                     getWarehouses()
                    _visibleButtonBackLiveData.value = false
                }
            }
        }
    }

    fun isStateCarNumber(): Boolean {
        return navigationStateOrder.value is CourierOrdersNavigationState.NavigateToCarNumber
    }

    fun toRegistrationClick() {
        _navigationState.value = CourierWarehousesNavigationState.NavigateToRegistration
    }

    private fun setLoaderForOrder(state: WaitLoaderForOrder) {
        _waitLoaderForOrder.value = state
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.value = state
    }

    fun getWarehouses() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                interactor.deleteWarehouses()
                val response = interactor.getWarehouses()
                val warehousesLocalEntity = setDataForCourierWarehousesDataBase(response)
                _warehouseState.value = CourierWarehouseItemState.Success
                setLoader(WaitLoader.Complete)
                getWarehousesComplete(warehousesLocalEntity)
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


    fun onCloseOrderDetailsClick(height: Int) {
        this.height = height
        withSelectedRowOrder(makeOrderAddresses())
        _navigationStateOrder.value = CourierOrdersNavigationState.CloseAddressesDetail
    }

    fun onAddressesClick() {
        _navigationStateOrder.value = CourierOrdersNavigationState.NavigateToAddresses
    }



    private fun makeOrderAddresses(): (rowOrder: Int) -> Unit = {
        _navigationStateOrder.value = CourierOrdersNavigationState.NavigateToOrders
//        interactor.mapState(
//            CourierMapState.UpdateMarkersWithAnimateToPosition(
//                pointsShow = orderMapMarkers,
//                pointsFrom = addressMapMarkers,
//                pointTo = orderMapMarkerWithoutWarehouse(it),
//                animateTo = boundingBoxWithOrderCenterGroupWarehouseCoordinatePoint(),
//                offsetY = offsetY(height)
//            )
//        )
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
        loc2.latitude = myLocation!!.latitude//1
        loc2.longitude = myLocation!!.longitude
        return (loc1.distanceTo(loc2) / 1000).roundToInt()
    }

    private fun getWarehousesComplete(it: Set<CourierWarehouseLocalEntity>) {
        sortedWarehouseEntitiesByCourierLocation(it)
        convertAndSaveItemsPointsMarkers()

        updateMyLocation()

    }


    private fun getWarehousesError(it: Throwable) {
        setLoader(WaitLoader.Complete)
        if (it is NoInternetException || it is TimeoutException || it is UnknownHostException) {
            _warehouseState.value = CourierWarehouseItemState.NoInternet
        } else {
            errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
        }
    }


    private fun sortedWarehouseEntitiesByCourierLocation(it: Set<CourierWarehouseLocalEntity>) {
        warehouseEntities =
            it.sortedBy { warehouse -> warehouse.distanceFromUser }.toMutableSet()
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
                item.longitude,
                PointType.WAREHOUSE
            )
            val mapMarker = Empty(mapPoint, resourceProvider.getWarehouseMapIcon())
            mapMarkers.add(mapMarker)
        }
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
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
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
        val id = mapPoint.id.split(" ")[0]
        viewModelScope.launch {
            if (id != MY_LOCATION_ID && mapPoint.type == PointType.WAREHOUSE && !mapPoint.id.split(" ")[0].startsWith(
                    CourierMapFragment.ADDRESS_MAP_PREFIX
                )
            ) {
                selectedMapPointForFragment1 = mapPoint
                val indexItemClick = id.toIntOrNull() ?: return@launch
                changeSelectedMapPoint(mapPoint)
                updateMarkers()
                val isMapSelected = isMapSelected(indexItemClick)
                changeSelectedWarehouseItemsByMap(indexItemClick, isMapSelected)
                val currentWarehouse =
                    interactor.loadWarehousesFromId(warehouseItems.elementAt(indexItemClick).id)
                changeShowDetailsOrder(isMapSelected, currentWarehouse)
            } else if (mapPoint.id.split(" ")[0].startsWith(CourierMapFragment.ADDRESS_MAP_PREFIX)) {
                addressMapClickForOrder(mapPoint)
            } else if (mapPoint.id != CourierMapFragment.WAREHOUSE_ID) {
                mapPointAfterCarNumber = mapPoint
                orderMapClick(mapPoint)
            }
        }
    }

    private fun orderMapClick(mapPoint: MapPoint) {
        val itemIndex = mapPoint.id.split(" ")[0].toInt() - 1
        saveRowOrder(itemIndex)
        val isSelected = changeSelectedOrderItems(itemIndex)
        changeMapMarkersForOrder(itemIndex, isSelected)
        updateOrderAndWarehouseMarkers()
        clearMap()
        scrollToForOrder(itemIndex)
        onNextFabForOrder()
    }

    fun onChangeCarNumberClick() {
        withSelectedRowOrder(navigateToEditCarNumber())
    }

    private fun navigateToEditCarNumber(): (rowOrder: Int) -> Unit = {
        _navigationStateOrder.value =
            CourierOrdersNavigationState.NavigateToCarNumber(
                result = CourierCarNumberResult.Edit(it)

            )
    }

    fun onConfirmTakeOrderClick() {
        when {
            interactor.isDemoMode() -> navigateToRegistrationDialog()
            interactor.carNumberIsConfirm() -> {
                withSelectedRowOrder(
                    navigateToDialogConfirmScoreInfo()
                )
            }
            else -> withSelectedRowOrder(navigateToCreateCarNumber())
        }
    }

    private fun navigateToCreateCarNumber(): (rowOrder: Int) -> Unit = {
        _navigationStateOrder.value =
            CourierOrdersNavigationState.NavigateToCarNumber(
                result = CourierCarNumberResult.Create(it)
            )

    }

    private fun navigateToDialogConfirmScoreInfo(): (rowOrder: Int) -> Unit = {
        var boxCountWithRouteId = 0
        with(orderLocalDataEntities[it]) {
            viewModelScope.launch {
                try {
                    if (courierOrderLocalEntity.ridMask != 0L) {
                        boxCountWithRouteId =
                            interactor.getBoxCountWithRidMask(courierOrderLocalEntity).count
                    }
                    _navigateToDialogConfirmScoreInfo.value =
                        NavigateToDialogConfirmInfo(
                            DialogInfoStyle.INFO.ordinal,
                            resourceProvider.getConfirmTitleDialog(courierOrderLocalEntity.id),
                            resourceProvider.getConfirmMessageDialog(
                                CarNumberUtils(interactor.carNumber()).fullNumber(),
                                resourceProvider.getCargo(
                                    courierOrderLocalEntity.minVolume,
                                    courierOrderLocalEntity.minBoxesCount
                                ),
                                courierOrderLocalEntity.reservedDuration,
                                if (boxCountWithRouteId == 0) courierOrderLocalEntity.minBoxesCount
                                else boxCountWithRouteId
                            ),
                            resourceProvider.getConfirmPositiveDialog(),
                            resourceProvider.getConfirmNegativeDialog()
                        )
                } catch (e: Exception) {
                    initOrdersError(e)
                }

            }
        }
    }

    private fun navigateToRegistrationDialog() {
        _navigationStateOrder.value =
            CourierOrdersNavigationState.NavigateToRegistrationDialog

    }

    private fun withSelectedRowOrder(action: (rowOrder: Int) -> Unit) {
        action(interactor.selectedRowOrder())
    }

    fun onMapClickWithDetail() {
        _navigationStateOrder.value = CourierOrdersNavigationState.CloseAddressesDetail
        unselectedAddressMapMarkers()
        updateAddressMarkers()
        unselectedAddressItems()
    }

    private fun unselectedAddressItems() {
        orderAddressItems.forEach { it.isSelected = false }
    }

    private fun unselectedAddressMapMarkers() {
        addressMapMarkers.forEachIndexed { index, courierMapMarker ->
            courierMapMarker.icon = if (orderAddressItems.elementAt(index).isUnspentTimeWork)
                resourceProvider.getOfficeMapTimeIcon() else resourceProvider.getOfficeMapIcon()
        }
    }

    private fun onNextFabForOrder() {
        _navigationStateOrder.value =
            CourierOrdersNavigationState.NavigateToOrderDetails(interactor.isDemoMode())
        initOrderDetails(interactor.selectedRowOrder())
    }

    private fun initOrderDetails(itemIndex: Int) {
        with(orderLocalDataEntities[itemIndex]) {
            initOrderDetails(itemIndex, courierOrderLocalEntity, dstOffices.size)
            convertAndSaveOrderAddressMapMarkersAndItems(dstOffices)
            removeWarehouseFromAddressMapMarker()
        }
        val hideOrderMarkers = orderMapMarkers.toMutableList()
        //hideOrderMarkers.removeFirst()
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

    fun onConfirmOrderClick() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                interactor.anchorTask()
                anchorTaskComplete()
            } catch (e: Exception) {
                anchorTaskError(e)
            } finally {
                val localOderEntity = interactor.courierLocalOrderEntity()
                 logCourierAndOrderData(localOderEntity)
            }
        }
    }

    private fun anchorTaskError(it: Throwable) {
        setLoader(WaitLoader.Complete)
        if (it is HttpException) {
            if (it.code() == COURIER_ONLY_ONE_TASK_ERROR_400) {
                _navigationStateOrder.value = CourierOrdersNavigationState.CourierLoader
            }
        } else if (it is BadRequestException) {
            if (it.error.code == COURIER_ONLY_ONE_TASK_ERROR) {
                _navigationStateOrder.value = CourierOrdersNavigationState.CourierLoader
            } else if (it.error.code == COURIER_TASK_ALREADY_RESERVED_ERROR) {
                taskRejected()
            }
        } else if (it is HttpObjectNotFoundException) {
            taskRejected()
        } else {
            errorDialogManager.showErrorDialog(
                it,
                _navigateToDialogInfo,
                DialogInfoFragment.DIALOG_INFO2_TAG
            )
        }
    }

    private fun taskRejected() {
        val ex = CustomException(resourceProvider.getTaskReject())
        errorDialogManager.showErrorDialog(ex, _navigateToDialogInfo)
    }

    fun onRegistrationConfirmClick() {
        _navigationStateOrder.value = CourierOrdersNavigationState.NavigateToRegistration
    }

    private fun anchorTaskComplete() {
        setLoader(WaitLoader.Complete)
        _navigationStateOrder.value = CourierOrdersNavigationState.NavigateToTimer
    }

    private fun addressesBoundingBox() =
        MapEnclosingCircle().allCoordinatePointToBoundingBox(addressCoordinatePoints.toMutableSet())

    private fun removeWarehouseFromAddressMapMarker() {
        addressMapMarkers.removeAt(WAREHOUSE_FIRST_INDEX)
    }

    private fun convertAndSaveOrderAddressMapMarkersAndItems(dstOffices: List<CourierOrderDstOfficeLocalEntity>) {
        val addressItems = mutableSetOf<CourierOrderDetailsAddressItem>()
        val addressCoordinatePoints = mutableListOf<CoordinatePoint>()
        val addressMapMarkers = mutableListOf<CourierMapMarker>()
        val warehouseLatitude = oldEntity?.latitude
        val warehouseLongitude = oldEntity?.longitude
        val warehouseMapPoint = MapPoint(
            CourierMapFragment.WAREHOUSE_ID,
            warehouseLatitude!!,
            warehouseLongitude!!,
            null
        )
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        addressMapMarkers.add(warehouseMapMarker)
        dstOffices.forEachIndexed { index, item ->
            addressItems.add(
                CourierOrderDetailsAddressItem(
                    if (item.isUnusualTime) resourceProvider.getOfficeMapTimeIcon() else resourceProvider.getOfficeMapIcon(),
                    item.name,
                    false,
                    item.isUnusualTime,
                    item.workTimes.splitTimes()
                )
            )
            addressCoordinatePoints.add(CoordinatePoint(item.latitude, item.longitude))
            val mapPoint =
                MapPoint(
                    "${CourierMapFragment.ADDRESS_MAP_PREFIX}$index",
                    item.latitude,
                    item.longitude,
                    null
                )
            val mapMarker = Empty(
                mapPoint,
                if (item.isUnusualTime) resourceProvider.getOfficeMapTimeIcon() else resourceProvider.getOfficeMapIcon()
            )
            addressMapMarkers.add(mapMarker)
        }
        saveAddressItems(addressItems)
        saveAddressCoordinatePoints(addressCoordinatePoints)
        saveAddressMapMarkers(addressMapMarkers)
    }

    private fun saveAddressItems(items: MutableSet<CourierOrderDetailsAddressItem>) {
        orderAddressItems = items
    }

    private fun saveAddressCoordinatePoints(coordinatePoints: List<CoordinatePoint>) {
        this.addressCoordinatePoints = coordinatePoints.toMutableList()
    }

    private fun saveAddressMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.addressMapMarkers = mapMarkers.toMutableList()
    }


    private fun String.splitTimes(): String {
        val sb = StringBuffer()
        this.split(TIME_DIVIDER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEachIndexed { index, item ->
                if (index > 0) sb.append("\n")
                sb.append(item)
            }
        return sb.toString()
    }

    private fun initOrderDetails( // text in order details
        idView: Int,
        courierOrderEntity: CourierOrderLocalEntity,
        pvz: Int
    ) {
        with(courierOrderEntity) {
            val carNumber = carNumberFormat(interactor.carNumber())
            val carTypeIcon = resourceProvider.getTypeIcons(interactor.carType())
            val itemId = (idView + 1).toString()
            // val coast = DecimalFormat("#,###.##").format(minCost)
            _orderDetails.value =
                CourierOrderDetailsInfoUIState.InitOrderDetails(
                    carNumber = carNumber,
                    carTypeIcon = carTypeIcon,
                    isChangeCarNumber = interactor.carNumberIsConfirm(),
                    itemId = itemId,
                    orderId = resourceProvider.getOrder(id),
                    cost = resourceProvider.getCost(minCost),
                    cargo = resourceProvider.getCargo(minBoxesCount),
                    countPvz = resourceProvider.getCountPvz(pvz),
                    reserve = resourceProvider.getArrive(reservedDuration),
                    taskDistance = taskDistance
                )
        }
    }

    private fun carNumberFormat(it: String) =
        it.let {
            if (it.isEmpty()) CarNumberState.Empty
            else CarNumberState.Indicated(resourceProvider.getCarNumber(CarNumberUtils(it).fullNumber()))
        }



    private fun changeMapMarkersForOrder(clickItemIndex: Int, isSelected: Boolean) {
        orderMapMarkers.filter { it.point.id != CourierMapFragment.WAREHOUSE_ID }
            .forEachIndexed { index, item ->
                item.icon = if (index == clickItemIndex) {
                    if (isSelected) {
                        resourceProvider.getOrderMapSelectedIcon()
                    } else {
                        resourceProvider.getOrderMapIcon()
                    }
                } else {
                    resourceProvider.getOrderMapIcon()
                }
            }
    }

    private fun changeSelectedOrderItems(itemIndex: Int): Boolean {
        var isSelected = false
        orderItems.forEachIndexed { index, item ->
            val orderItem = (item as CourierOrderItem)
            orderItem.isSelected = if (index == itemIndex) {
                isSelected = !orderItem.isSelected
                isSelected
            } else false
        }
        return isSelected
    }

    private fun saveRowOrder(itemIndex: Int) {
        interactor.saveRowOrder(itemIndex)
    }

    private fun scrollToForOrder(index: Int) {
        interactor.mapState(
            CourierMapState.NavigateToPoint(
                CoordinatePoint(
                    orderMapMarkers.elementAt(index).point.lat,
                    orderMapMarkers.elementAt(index).point.long
                )
            )
        )
    }


    private fun addressMapClickForOrder(mapPoint: MapPoint) {
        changeSelectedAddressMapPointAndItemByMap(mapPoint.id.split(" ")[0])
        updateAddressMarkers()
        updateShowingAddressDetail(getIdMapWithoutPrefix(mapPoint))
    }

    private fun getIdMapWithoutPrefix(mapPoint: MapPoint) =
        mapPoint.id.split(" ")[0].replace(CourierMapFragment.ADDRESS_MAP_PREFIX, "").toInt()

    private fun updateShowingAddressDetail(idMapClick: Int) {
        val address = orderAddressItems.elementAt(idMapClick)
        _navigationStateOrder.value =
            if (address.isSelected) {
                CourierOrdersNavigationState.ShowAddressDetail(
                    if (address.isUnspentTimeWork) resourceProvider.getOfficeMapSelectedTimeIcon() else resourceProvider.getOfficeMapSelectedIcon(),
                    address.fullAddress,
                    address.timeWork,
                )
            } else {
                CourierOrdersNavigationState.CloseAddressesDetail
            }
    }

    fun getOrderAddressItems() = orderAddressItems

    private fun updateAddressMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(addressMapMarkers.toMutableSet()))
    }

    private fun changeSelectedAddressMapPointAndItemByMap(mapPointId: String) {
        addressMapMarkers.forEachIndexed { index, item ->
            val addressItem = orderAddressItems.elementAt(index)
            item.icon =
                if (item.point.id == mapPointId && (item.icon == resourceProvider.getOfficeMapIcon() || item.icon == resourceProvider.getOfficeMapTimeIcon())) {
                    addressItem.isSelected = true
                    if (addressItem.isUnspentTimeWork) resourceProvider.getOfficeMapSelectedTimeIcon() else resourceProvider.getOfficeMapSelectedIcon()
                } else {
                    addressItem.isSelected = false
                    if (addressItem.isUnspentTimeWork) resourceProvider.getOfficeMapTimeIcon() else resourceProvider.getOfficeMapIcon()
                }
        }
    }

    private fun changeSelectedMapPoint(mapPoint: MapPoint) {
        mapMarkers.forEach { item ->
            item.icon =
                if (item.point.id == mapPoint.id.split(" ")[0] &&
                    item.icon == resourceProvider.getWarehouseMapIcon()
                ) {
                    interactor.mapState(
                        CourierMapState.NavigateToPoint(
                            CoordinatePoint(
                                mapPoint.lat,
                                mapPoint.long
                            )
                        )
                    )
                    resourceProvider.getWarehouseMapSelectedIcon()
                } else if (item.point.id == mapPoint.id.split(" ")[0] &&
                    item.icon == resourceProvider.getWarehouseMapSelectedIcon()
                ) {
                    resourceProvider.getWarehouseMapIcon()
                } else {
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


    private fun isMapSelected(indexItemClick: Int) =
        mapMarkers.elementAt(indexItemClick).icon == resourceProvider.getWarehouseMapSelectedIcon()

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.UpdateMyLocationPoint(myLocation!!))
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

    private fun changeShowDetailsOrder(
        selected: Boolean,
        warehouseItem: List<CourierWarehouseLocalEntity>?
    ) {
        if (selected) {
            val distance =
                resourceProvider.getDistance(warehouseItem?.get(0)?.distanceFromUser.toString())
            _showOrdersState.value =
                CourierWarehousesShowOrdersState.Enable(warehouseItem, distance)
        } else {
            _showOrdersState.value = CourierWarehousesShowOrdersState.Disable
        }
    }







    fun onNextFab(height: Int) {
        setLoaderForOrder(WaitLoaderForOrder.Wait)
        viewModelScope.launch {
            try {
                screenHeight = height
                val index = warehouseItems.indexOfFirst { item ->
                    item.isSelected
                }
                assert(index != -1)
                clearFabAndWhList()
                oldEntity = warehouseEntities.elementAt(index).copy()
                val oldEntity = oldEntity!!
                interactor.clearAndSaveCurrentWarehouses(oldEntity)
                navigateToCourierOrders(oldEntity)
                orderLocalDataEntities =
                    interactor.freeOrdersLocalClearAndSave(oldEntity.id)
                initOrdersComplete(height)
                delay(1000)
                setLoaderForOrder(WaitLoaderForOrder.Complete)
            } catch (e: Exception) {
                initOrdersError(e)
                setLoaderForOrder(WaitLoaderForOrder.Complete)
            }

        }
    }

    private fun initOrdersComplete(height: Int) {

        convertAndSaveOrderPointMarkers(orderLocalDataEntities) //
        setLoader(WaitLoader.Complete)
        ordersComplete(height)
    }

    private fun ordersComplete(height: Int) {
        if (orderItems.isNotEmpty()) {
            clearMap()
            updateOrderAndWarehouseMarkers()//height
            zoomAllGroupMarkersFromBoundingBox(height)

        }
    }

    private fun updateOrderAndWarehouseMarkers() {
        val warehouseMapMarker = mutableSetOf(orderMapMarkers.first())
        val orders = orderMapMarkers.apply { remove(first()) }
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
        val lastElement = orderCenterGroupPoints.last()
        orderCenterGroupPoints.remove(lastElement)
        return boundingBox
    }

    private fun warehouseCoordinatePoint() =
        CoordinatePoint(oldEntity?.latitude!!, oldEntity?.longitude!!)

    private fun clearMap() {
        interactor.mapState(CourierMapState.ClearMap)
    }

    private fun convertAndSaveOrderPointMarkers(orders: List<CourierOrderLocalDataEntity>) {
        val warehouseItem = oldEntity!!
        val orderItems = mutableListOf<BaseItem>()
        val orderCenterGroupPoints = mutableListOf<CoordinatePoint>()
        val orderMapMarkers = mutableListOf<CourierMapMarker>()
        val warehouseLatitude = warehouseItem.latitude
        val warehouseLongitude = warehouseItem.longitude
        val warehouseMapPoint =
            MapPoint(CourierMapFragment.WAREHOUSE_ID, warehouseLatitude, warehouseLongitude, null)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        orderMapMarkers.add(warehouseMapMarker)
        orders.forEachIndexed { index, item ->
            val idPoint = (index + 1).toString()


            val orderItem = dataBuilder.buildOrderItem(idPoint, index, item, false)
            orderItems.add(orderItem)
            val coordinatePoints = mutableListOf<CoordinatePoint>()
            item.dstOffices.forEach { dstOffices ->
                coordinatePoints.add(CoordinatePoint(dstOffices.latitude, dstOffices.longitude))
            }
            val boundingBox =
                MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints.toMutableSet())
            val centerGroupPoint =
                CoordinatePoint(boundingBox.centerLatitude, boundingBox.centerLongitude)
            orderCenterGroupPoints.add(centerGroupPoint)
            val mapPoint =
                MapPoint(idPoint, centerGroupPoint.latitude, centerGroupPoint.longitude, null)
            val mapMarker = Empty(mapPoint, resourceProvider.getOrderMapIcon())
            orderMapMarkers.add(mapMarker)
        }
        this.orderItems = orderItems.toMutableSet()
        this.orderCenterGroupPoints = orderCenterGroupPoints.toMutableSet()
        this.orderMapMarkers = orderMapMarkers.toMutableSet()
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
        changeShowDetailsOrder(false, null)

    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    private fun initOrdersError(it: Throwable) {
        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
        setLoader(WaitLoader.Complete)
    }

    companion object {
        const val SCREEN_TAG = "CourierWarehouses"
        const val RADIUS_KM = 30
        const val SCREEN_TAG_ORDERS = "CourierOrders"
        const val WAREHOUSE_FIRST_INDEX = 0
        const val DETAILS_HEIGHT = -400
        const val TIME_DIVIDER = ";"

    }

}

