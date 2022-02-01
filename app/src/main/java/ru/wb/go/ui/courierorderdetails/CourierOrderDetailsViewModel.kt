package ru.wb.go.ui.courierorderdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapFragment
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierorderdetails.domain.AnchorTaskStatus
import ru.wb.go.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import java.text.DecimalFormat

class CourierOrderDetailsViewModel(
    private val parameters: CourierOrderDetailsParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrderDetailsInteractor,
    private val resourceProvider: CourierOrderDetailsResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

//    private val _toolbarLabelState = MutableLiveData<Label>()
//    val toolbarLabelState: LiveData<Label>
//        get() = _toolbarLabelState

//    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
//    val toolbarNetworkState: LiveData<NetworkState>
//        get() = _toolbarNetworkState

//    private val _versionApp = MutableLiveData<String>()
//    val versionApp: LiveData<String>
//        get() = _versionApp

    private val _orderInfo = MutableLiveData<CourierOrderDetailsInfoUIState>()
    val orderInfo: LiveData<CourierOrderDetailsInfoUIState>
        get() = _orderInfo

    private val _orderDetails = MutableLiveData<CourierOrderDetailsUIState>()
    val orderDetails: LiveData<CourierOrderDetailsUIState>
        get() = _orderDetails

    private val _navigationState = SingleLiveEvent<CourierOrderDetailsNavigationState>()
    val navigationState: LiveData<CourierOrderDetailsNavigationState>
        get() = _navigationState

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _navigateToTaskIsNotExistDialog = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToTaskIsNotExistDialog: LiveData<NavigateToDialogInfo>
        get() = _navigateToTaskIsNotExistDialog

    private val _navigateToDialogConfirmScoreInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmScoreInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmScoreInfo

    private val _progressState = MutableLiveData<CourierOrderDetailsProgressState>()
    val progressState: LiveData<CourierOrderDetailsProgressState>
        get() = _progressState

    private val _holdState = MutableLiveData<Boolean>()
    val holdState: LiveData<Boolean>
        get() = _holdState

    private var mapMarkers = mutableListOf<CourierMapMarker>()
    private var courierOrderDetailsItems = mutableListOf<CourierOrderDetailsItem>()

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

    private fun saveCourierOrderDetailsItems(items: List<CourierOrderDetailsItem>) {
        courierOrderDetailsItems = items.toMutableList()
    }

    init {
        onTechEventLog("init")
    }

    fun onUpdate() {
        onTechEventLog("onUpdate")
        initOrder()
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeOrderData()
                .subscribe(
                    { observeOrderDataComplete(it) },
                    { observeOrderDataError(it) })
        )
    }

    private fun observeOrderDataComplete(it: CourierOrderLocalDataEntity) {
        initOrderInfo(it.courierOrderLocalEntity, it.dstOffices.size)
        initOrderItems(it.dstOffices)
    }

    private fun observeOrderDataError(throwable: Throwable) {
        onTechErrorLog("onUpdate", throwable)
    }

    private fun initOrderInfo(courierOrderLocalEntity: CourierOrderLocalEntity, pvz: Int) {
        with(courierOrderLocalEntity) {
            onTechEventLog("initOrderInfo", "order id: $id pvz: $pvz")
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderDetailsInfoUIState.InitOrderInfo(
                carNumber = resourceProvider.getCarNumber(interactor.carNumber()),
                orderNumber = parameters.orderNumber,
                order = resourceProvider.getOrder(id),
                coast = resourceProvider.getCoast(coast),
                countBox = resourceProvider.getBoxCountBox(minBoxesCount),
                volume = resourceProvider.getVolume(minVolume),
                countPvz = resourceProvider.getCountPvz(pvz),
                arrive = resourceProvider.getArrive(courierOrderLocalEntity.reservedDuration)
            )
        }
    }

    private fun initOrderItems(dstOffices: List<CourierOrderDstOfficeLocalEntity>) {
        val items = mutableListOf<CourierOrderDetailsItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val mapMarkers = mutableListOf<CourierMapMarker>()


        val warehouseLatitude = parameters.warehouseLatitude
        val warehouseLongitude = parameters.warehouseLongitude

        val warehouseMapPoint =
            MapPoint(CourierMapFragment.WAREHOUSE_ID, warehouseLatitude, warehouseLongitude)
        val warehouseMapMarker =
            Empty(warehouseMapPoint, resourceProvider.getWarehouseMapSelectedIcon())
        mapMarkers.add(warehouseMapMarker)
        coordinatePoints.add(CoordinatePoint(warehouseLatitude, warehouseLongitude))

        dstOffices.forEachIndexed { index, item ->
            items.add(CourierOrderDetailsItem(item.name, false))
            coordinatePoints.add(CoordinatePoint(item.latitude, item.longitude))
            val mapPoint = MapPoint(index.toString(), item.latitude, item.longitude)
            val mapMarker = Empty(mapPoint, resourceProvider.getOfficeMapIcon())
            mapMarkers.add(mapMarker)
        }
        saveCourierOrderDetailsItems(items)
        initItems(items)
        saveMapMarkers(mapMarkers)
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        LogUtils { logDebugApp("coordinatePoints " + coordinatePoints.toString()) }
        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints)
        interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, false))
    }

    private fun initItems(items: MutableList<CourierOrderDetailsItem>) {
        _orderDetails.value = if (items.isEmpty()) CourierOrderDetailsUIState.Empty
        else CourierOrderDetailsUIState.InitItems(items)
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
    }

    fun onChangeCarNumberClick() {
        onTechEventLog("onChangeCarNumberClick")
        with(parameters) {
            _navigationState.value = CourierOrderDetailsNavigationState.NavigateToCarNumber(
                title, orderNumber, order, warehouseLatitude, warehouseLongitude
            )
        }
    }

    fun confirmTakeOrderClick() {

        _navigateToDialogConfirmScoreInfo.value = NavigateToDialogConfirmInfo(
            DialogInfoStyle.INFO.ordinal,
            resourceProvider.getConfirmTitleDialog(parameters.order.id),
            resourceProvider.getConfirmMessageDialog(
                interactor.carNumber(),
                parameters.order.minVolume,
                parameters.order.reservedDuration
            ),
            resourceProvider.getConfirmPositiveDialog(),
            resourceProvider.getConfirmNegativeDialog()
        )
    }

    private fun anchorTaskComplete(anchorTaskStatus: AnchorTaskStatus) {
        when (anchorTaskStatus) {
            AnchorTaskStatus.AnchorTaskComplete -> {
                onTechEventLog("anchorTaskComplete", "AnchorTaskComplete")
                _navigationState.value = CourierOrderDetailsNavigationState.NavigateToTimer
            }
            AnchorTaskStatus.TaskIsNotExist -> {
                onTechEventLog("anchorTaskComplete", "TaskIsNotExist")
                showIsNotExistDialog()
            }
        }
        _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
        unlockState()
    }

    private fun showIsNotExistDialog() {
        _navigateToTaskIsNotExistDialog.value = NavigateToDialogInfo(
            DialogInfoStyle.WARNING.ordinal,
            resourceProvider.getOrderIsNotExistTitleDialog(),
            resourceProvider.getOrderIsNotExistMessageDialog(),
            resourceProvider.getOrderIsNotExistButtonDialog()
        )
    }

    private fun anchorTaskError(throwable: Throwable) {
        onTechErrorLog("anchorTaskError", throwable)
        courierWarehouseError(throwable)
        unlockState()
    }

    private fun courierWarehouseError(throwable: Throwable) {
        progressComplete()
        _navigateToDialogInfo.value = messageError(throwable, resourceProvider)
    }

    private fun actionProgress() = Completable.fromAction {
        _progressState.value = CourierOrderDetailsProgressState.Progress
    }

    private fun lockState() {
        _holdState.value = true
    }

    private fun unlockState() {
        _holdState.value = false
    }

    fun onCancelLoadClick() {
        onTechEventLog("onCancelLoadClick")
        clearSubscription()
    }

    fun onItemClick(index: Int) {
        onTechEventLog("onItemClick")
        changeItemSelected(index)
    }

    private fun changeItemSelected(selectIndex: Int) {
        updateItems(selectIndex)
        navigateToPoint(selectIndex, courierOrderDetailsItems[selectIndex].isSelected)
    }

    private fun updateItems(selectIndex: Int) {
        courierOrderDetailsItems.forEachIndexed { index, item ->
            val isSelected = if (selectIndex == index) !item.isSelected else false
            val itemCheckSelected = courierOrderDetailsItems[index].copy(isSelected = isSelected)
            courierOrderDetailsItems[index] = itemCheckSelected
        }
        _orderDetails.value =
            if (courierOrderDetailsItems.isEmpty()) CourierOrderDetailsUIState.Empty
            else CourierOrderDetailsUIState.UpdateItems(selectIndex, courierOrderDetailsItems)
    }

    private fun navigateToPoint(selectIndex: Int, isSelected: Boolean) {
        mapMarkers.forEach { item ->
            item.icon = if (item.point.id == selectIndex.toString()) {
                if (isSelected) resourceProvider.getOfficeMapSelectedIcon() else resourceProvider.getOfficeMapIcon()
            } else {
                resourceProvider.getOfficeMapIcon()
            }
        }
        mapMarkers.find { it.point.id == selectIndex.toString() }?.apply {
            mapMarkers.remove(this)
            mapMarkers.add(this)
        }
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.NavigateToMarker(selectIndex.toString()))
    }

    fun onConfirmOrderClick() {
        lockState()
//        showProgress()
        onTechEventLog("onConfirmOrderClick")
        // TODO: 27.01.2022 реализовать проверку наличия заказа
        addSubscription(
            actionProgress()
                .andThen(interactor.anchorTask())
                .subscribe(
                    { anchorTaskComplete(it) },
                    { anchorTaskError(it) })
        )
    }

    fun onCancelOrderClick() {
        onTechEventLog("onCancelOrderClick")
    }

    fun onTaskNotExistConfirmClick() {
        _navigationState.value = CourierOrderDetailsNavigationState.NavigateToBack
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrderDetails"
    }

}