package ru.wb.go.ui.courierintransit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransit.delegates.items.*
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import ru.wb.go.utils.time.DateTimeFormatter

class CourierIntransitViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierIntransitInteractor,
    private val resourceProvider: CourierIntransitResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigateToErrorDialog = SingleLiveEvent<ErrorDialogData>()
    val navigateToErrorDialog: LiveData<ErrorDialogData>
        get() = _navigateToErrorDialog

    private val _navigateToDialogConfirmInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmInfo

    private val _intransitOrders = MutableLiveData<CourierIntransitItemState>()
    val intransitOrders: LiveData<CourierIntransitItemState>
        get() = _intransitOrders

    private val _navigationState = SingleLiveEvent<CourierIntransitNavigationState>()
    val navigationState: LiveData<CourierIntransitNavigationState>
        get() = _navigationState

    private val _beepEvent =
        SingleLiveEvent<CourierIntransitScanOfficeBeepState>()
    val beepEvent: LiveData<CourierIntransitScanOfficeBeepState>
        get() = _beepEvent

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _intransitTime = MutableLiveData<CourierIntransitTimeState>()
    val intransitTime: LiveData<CourierIntransitTimeState>
        get() = _intransitTime

    private val _isEnableState = SingleLiveEvent<Boolean>()
    val isEnableBottomState: LiveData<Boolean>
        get() = _isEnableState

    private var intransitItems = mutableListOf<BaseIntransitItem>()
    private var mapMarkers = mutableListOf<IntransitMapMarker>()
    private var coordinatePoints = mutableListOf<CoordinatePoint>()

    private fun copyItems(items: List<BaseIntransitItem>) {
        intransitItems = items.toMutableList()
    }

    private fun copyMarkers(items: List<IntransitMapMarker>) {
        mapMarkers = items.toMutableList()
    }

    init {
        onTechEventLog("initIntransit")
        initTime()
        observeMapAction()
    }

    fun update() {
        initTitle()
        observeOffices()
    }

    private fun initTitle() {
        _toolbarLabelState.value = Label(resourceProvider.getLabelId(interactor.getOrderId()))
    }

    private fun observeOffices() {
        addSubscription(
            interactor.getOffices()
                .subscribe({ initOfficesComplete(it) },
                    {
                        onTechErrorLog("Get Offices", it)
                        errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
                    })
        )
    }

    private fun initTime() {
        addSubscription(
            interactor.observeOrderTimer()
                .subscribe({
                    _intransitTime.value = CourierIntransitTimeState.Time(
                        DateTimeFormatter.getDigitFullTime(it.toInt())
                    )
                }, {})
        )
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    private fun observeMapAction() {
        addSubscription(
            interactor.observeMapAction().subscribe(
                { observeMapActionComplete(it) },
                { onTechErrorLog("observeMapAction", it) }
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
        val mapPointClickId = mapPoint.id
        if (mapPointClickId != CourierMapFragment.MY_LOCATION_ID) {
            val indexItemClick = mapPointClickId.toInt()
            val isMarkerSelected = invertMarkerSelected(indexItemClick)
            changeSelectedMarkers(mapPointClickId, isMarkerSelected)
            updateMarkers()
            changeSelectedItemsByMarker(indexItemClick, isMarkerSelected)
            updateAndScrollToItems(indexItemClick)
        }
    }

    private fun invertMarkerSelected(indexItemClick: Int) =
        with(mapMarkers[indexItemClick]) { icon != getSelectedMapIcon(type) }


    private fun changeSelectedMarkers(mapPointClickId: String, isSelected: Boolean) {
        mapMarkers.forEach { item ->
            val normalIcon = getNormalMapIcon(item.type)
            item.icon =
                if (item.point.id == mapPointClickId)
                    if (isSelected) getSelectedMapIcon(item.type) else normalIcon
                else normalIcon
        }
    }

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
    }

    private fun changeSelectedItemsByMarker(indexItemClick: Int, isMarkerSelected: Boolean) {
        if (intransitItems.isEmpty()) return
        intransitItems.forEachIndexed { index, item ->
            item.isSelected = if (index == indexItemClick) isMarkerSelected
            else false
        }
    }

    private fun updateAndScrollToItems(indexItemClick: Int) {
        _intransitOrders.value =
            CourierIntransitItemState.UpdateItems(intransitItems.toMutableList())
        _intransitOrders.value = CourierIntransitItemState.ScrollTo(indexItemClick)
    }

    private fun initOfficesComplete(dstOffices: List<LocalOfficeEntity>) {
        onTechEventLog("initOfficesComplete", "dstOffices count " + dstOffices.size)
        val items = mutableListOf<BaseIntransitItem>()
        coordinatePoints = mutableListOf()
        val markers = mutableListOf<IntransitMapMarker>()

        var deliveredCountTotal = 0
        var fromCountTotal = 0
        dstOffices.forEachIndexed { index, office ->
            with(office) {
                deliveredCountTotal += deliveredBoxes
                fromCountTotal += countBoxes
                val item: BaseIntransitItem
                val mapMarker: CourierMapMarker

                val type = itemType(deliveredBoxes, countBoxes, isVisited, isOnline)
                val iconMap = getNormalMapIcon(type)

                when (type) {
                    IntransitItemType.Empty -> {
                        item = CourierIntransitEmptyItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index,
                        )
                    }
                    IntransitItemType.UnloadingExpects -> {
                        item = CourierIntransitUnloadingExpectsItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                    }
                    IntransitItemType.FailedUnloadingAll -> {
                        item = CourierIntransitUndeliveredAllItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                    }
                    IntransitItemType.Complete -> {
                        item = CourierIntransitCompleteItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                    }
                }

                val mapPoint = MapPoint(index.toString(), latitude, longitude)
                mapMarker = Intransit(mapPoint, iconMap, type)

                items.add(item)
                coordinatePoints.add(CoordinatePoint(latitude, longitude))

                markers.add(mapMarker)
            }
        }
        copyItems(items)
        val boxTotalCount =
            resourceProvider.getBoxCountAndTotal(deliveredCountTotal, fromCountTotal)
        initItems(items, boxTotalCount)

        copyMarkers(markers)
        initMap(markers, coordinatePoints)

        if (deliveredCountTotal == fromCountTotal)
            _intransitOrders.value = CourierIntransitItemState.CompleteDelivery
    }

    private fun initItems(items: MutableList<BaseIntransitItem>, boxTotal: String) {
        _intransitOrders.value = if (items.isEmpty()) CourierIntransitItemState.Empty
        else CourierIntransitItemState.InitItems(items, boxTotal)
    }

    private fun initMap(
        mapMarkers: MutableList<IntransitMapMarker>,
        coordinatePoints: MutableList<CoordinatePoint>
    ) {
        if (mapMarkers.isEmpty()) {
            interactor.mapState(CourierMapState.NavigateToPoint(moscowCoordinatePoint()))
        } else {
            interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
            val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints)
            interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
        }
    }

    fun onScanQrPvzClick() {

        onTechEventLog("Button scan QR Office")
        changeSelectedMarkers("1", false)
        updateMarkers()
        changeSelectedItemsByMarker(0, false)
        updateAndScrollToItems(0)
        _navigationState.value = CourierIntransitNavigationState.NavigateToScanner
    }

    fun onCompleteDeliveryClick() {
        onTechEventLog("Button CompleteDelivery")
        _isEnableState.value = false
        val boxes = interactor.getBoxes()
        val order = interactor.getOrder()
        val orderId = order.orderId.toString()

        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.setIntransitTask(orderId, boxes)
                .concatWith(interactor.completeDelivery(order))
                .subscribe(
                    { completeDeliveryComplete(order.cost) },
                    {
                        onTechErrorLog("CompleteDelivery", it)
                        setLoader(WaitLoader.Complete)
                        errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
                    })
        )
    }

    private fun completeDeliveryComplete(cost: Int) {
        setLoader(WaitLoader.Complete)
        val boxes = interactor.getBoxes()
        val cdr = CompleteDeliveryResult(
            boxes.filter { box -> box.deliveredAt != "" }.size,
            boxes.size,
            cost
        )
        onTechEventLog(
            "CompleteDelivery",
            "boxes: ${cdr.countBoxes}/${cdr.deliveredBoxes} - ${cdr.cost} руб"
        )

        interactor.clearLocalTaskData()
        _navigationState.value = CourierIntransitNavigationState.NavigateToCompleteDelivery(
            cdr.cost,
            cdr.deliveredBoxes,
            cdr.countBoxes
        )
        clearSubscription()
    }

    fun onErrorDialogConfirmClick() {
        _isEnableState.value = true
    }

    fun onItemOfficeClick(selectIndex: Int) {
        onTechEventLog("Select Office index=$selectIndex")
        changeSelectedItems(selectIndex)
        updateItems()
        val isSelected = intransitItems[selectIndex].isSelected
        changeSelectedMarkers(isSelected, selectIndex)
        updateMarkers(isSelected, selectIndex)
    }

    private fun changeSelectedItems(selectIndex: Int) {
        intransitItems.forEachIndexed { index, item ->
            intransitItems[index].isSelected =
                if (selectIndex == index) !item.isSelected
                else false
        }
    }

    private fun updateItems() {
        _intransitOrders.value =
            if (intransitItems.isEmpty()) CourierIntransitItemState.Empty
            else CourierIntransitItemState.UpdateItems(intransitItems)
    }

    private fun changeSelectedMarkers(isSelected: Boolean, selectIndex: Int) {
        mapMarkers.forEach { item ->
            with(item) {
                icon = if (point.id == selectIndex.toString()) {
                    if (isSelected) getSelectedMapIcon(type) else getNormalMapIcon(type)
                } else {
                    getNormalMapIcon(type)
                }
            }
        }
    }

    private fun updateMarkers(isSelected: Boolean, selectIndex: Int) {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        if (isSelected)
            interactor.mapState(CourierMapState.NavigateToMarker(selectIndex.toString()))
    }

    private fun getNormalMapIcon(type: IntransitItemType) =
        when (type) {
            IntransitItemType.Empty -> resourceProvider.getEmptyMapIcon()
            IntransitItemType.FailedUnloadingAll -> resourceProvider.getFailedUndeliveredAllMapIcon()
            IntransitItemType.UnloadingExpects -> resourceProvider.getUnloadingExpectsMapIcon()
            IntransitItemType.Complete -> resourceProvider.getCompleteMapIcon()
        }

    private fun getSelectedMapIcon(type: IntransitItemType) =
        when (type) {
            IntransitItemType.Empty -> resourceProvider.getEmptySelectedMapIcon()
            IntransitItemType.FailedUnloadingAll -> resourceProvider.getFailedUndeliveredAllSelectedMapIcon()
            IntransitItemType.UnloadingExpects -> resourceProvider.getUnloadingExpectsSelectedMapIcon()
            IntransitItemType.Complete -> resourceProvider.getCompleteSelectMapIcon()
        }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    fun onShowAllClick() {
        zoomMarkersFromBoundingBox()
    }

    private fun zoomMarkersFromBoundingBox() {
        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints)
        interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
    }

    data class Label(val label: String)

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    private fun itemType(
        deliveredBoxes: Int,
        countBoxes: Int,
        isVisited: Boolean,
        isOnline: Boolean
    ): IntransitItemType {
        return when (deliveredBoxes) {
            EMPTY_DELIVERED_BOX -> if (isVisited) IntransitItemType.FailedUnloadingAll else IntransitItemType.Empty
            countBoxes -> if (isOnline) IntransitItemType.Complete else IntransitItemType.UnloadingExpects
            else -> IntransitItemType.FailedUnloadingAll
        }
    }

    companion object {
        const val DEFAULT_SELECT_ITEM = false
        const val EMPTY_DELIVERED_BOX = 0
        const val SCREEN_TAG = "CourierIntransit"
    }

}

sealed class IntransitItemType {
    object Empty : IntransitItemType()
    object Complete : IntransitItemType()
    object UnloadingExpects : IntransitItemType()
    object FailedUnloadingAll : IntransitItemType()
}