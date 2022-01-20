package ru.wb.go.ui.courierorderdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriermap.CourierMapMarker
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.Empty
import ru.wb.go.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
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

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _orderInfo = MutableLiveData<CourierOrderDetailsInfoUIState>()
    val orderInfo: LiveData<CourierOrderDetailsInfoUIState>
        get() = _orderInfo

    private val _orderDetails = MutableLiveData<CourierOrderDetailsUIState>()
    val orderDetails: LiveData<CourierOrderDetailsUIState>
        get() = _orderDetails

    private val _navigationState = SingleLiveEvent<CourierOrderDetailsNavigationState>()
    val navigationState: LiveData<CourierOrderDetailsNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierOrderDetailsProgressState>()
    val progressState: LiveData<CourierOrderDetailsProgressState>
        get() = _progressState

    private var courierOrderDetailsItems = mutableListOf<CourierOrderDetailsItem>()

    private var mapMarkers = mutableListOf<CourierMapMarker>()

    private fun saveCourierOrderDetailsItems(items: List<CourierOrderDetailsItem>) {
        courierOrderDetailsItems = items.toMutableList()
    }

    private fun saveMapMarkers(mapMarkers: List<CourierMapMarker>) {
        this.mapMarkers = mapMarkers.toMutableList()
    }

    init {
        onTechEventLog("init")
        observeNetworkState()
        fetchVersionApp()
        initToolbar()
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    fun onUpdate() {
        onTechEventLog("onUpdate")
        initOrder()
    }

    private fun initToolbar() {
        _toolbarLabelState.value = Label(parameters.title)
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
                resourceProvider.getOrder(id),
                resourceProvider.getCoast(coast),
                resourceProvider.getArrive(courierOrderLocalEntity.reservedDuration),
                resourceProvider.getBoxCountAndVolume(minBoxesCount, minVolume),
                resourceProvider.getPvz(pvz)
            )
        }
    }

    private fun initOrderItems(dstOffices: List<CourierOrderDstOfficeLocalEntity>) {
        val items = mutableListOf<CourierOrderDetailsItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val mapMarkers = mutableListOf<CourierMapMarker>()
        dstOffices.forEachIndexed { index, item ->
            items.add(CourierOrderDetailsItem(index, item.name, false))
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
        val boundingBox = MapEnclosingCircle().minimumBoundingBox(coordinatePoints)
        interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, false))
    }

    private fun initItems(items: MutableList<CourierOrderDetailsItem>) {
        _orderDetails.value = if (items.isEmpty()) CourierOrderDetailsUIState.Empty
        else CourierOrderDetailsUIState.InitItems(items)
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
    }

    fun onTakeOrderClick() {
        onTechEventLog("onTakeOrderClick")
        _navigationState.value = if (interactor.carNumberIsConfirm()) {
            CourierOrderDetailsNavigationState.NavigateToOrderConfirm
        } else {
            CourierOrderDetailsNavigationState.NavigateToCarNumber(
                parameters.title,
                parameters.order
            )
        }
    }

    fun confirmTakeOrderClick() {

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

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrderDetails"
    }

    data class Label(val label: String)

}