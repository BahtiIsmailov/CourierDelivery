package ru.wb.go.ui.courierintransit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransit.delegates.items.*
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import ru.wb.go.utils.prefs.SharedWorker
import ru.wb.go.utils.time.DateTimeFormatter

class CourierIntransitViewModel(
    private val sharedWorker: SharedWorker,
    private val interactor: CourierIntransitInteractor,
    private val resourceProvider: CourierIntransitResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : ServicesViewModel(interactor, resourceProvider) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigateToErrorDialog = SingleLiveEvent<ErrorDialogData>()
    val navigateToErrorDialog: LiveData<ErrorDialogData>
        get() = _navigateToErrorDialog

    private val _navigatorState = SingleLiveEvent<CourierIntransitNavigatorUIState>()
    val navigatorState: LiveData<CourierIntransitNavigatorUIState>
        get() = _navigatorState


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

    private val _currentItemBackgroundForBottomSheet = MutableLiveData<IntransitItemType>()
    val currentItemBackgroundForBottomSheet: LiveData<IntransitItemType> =
        _currentItemBackgroundForBottomSheet

    private val _courierIntransitEmptyItemLiveData = MutableLiveData<CourierIntransitEmptyItem>()
    val courierIntransitEmptyItemLiveData: LiveData<CourierIntransitEmptyItem> =
        _courierIntransitEmptyItemLiveData

    private val _courierIntransitCompleteItemLiveData =
        MutableLiveData<CourierIntransitCompleteItem>()
    val courierIntransitCompleteItemLiveData: LiveData<CourierIntransitCompleteItem> =
        _courierIntransitCompleteItemLiveData

    private val _courierIntransitUndeliveredAllItemLiveData =
        MutableLiveData<CourierIntransitUndeliveredAllItem>()
    val courierIntransitUndeliveredAllItemLiveData: LiveData<CourierIntransitUndeliveredAllItem> =
        _courierIntransitUndeliveredAllItemLiveData

    private val _courierIntransitUnloadingExpectsItemLiveData =
        MutableLiveData<CourierIntransitUnloadingExpectsItem>()
    val courierIntransitUnloadingExpectsItemLiveData: LiveData<CourierIntransitUnloadingExpectsItem> =
        _courierIntransitUnloadingExpectsItemLiveData


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
        //getSchedule()
        //onTechEventLog("initIntransit")
        initTime()
        observeMapAction()
    }

    fun update() {
        initTitle()
        observeOffices()
    }

    private fun initTitle() {
        viewModelScope.launch {
            _toolbarLabelState.value = Label(resourceProvider.getLabelId(interactor.getOrderId()))
        }
    }

    private fun observeOffices() {
        interactor.getOffices()
            .onEach {
                initOfficesComplete(it)
            }
            .catch {
                logException(it,"observeOffices")
                //onTechEventLog("Get Offices", it)
                errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
            }
            .launchIn(viewModelScope)
    }


    private fun initTime() {
        interactor.observeOrderTimer()
            .onEach {
                _intransitTime.value = CourierIntransitTimeState.Time(
                    DateTimeFormatter.getDigitFullTime(it.toInt())
                )
            }
            .catch {
                logException(it,"initTime")
            }
            .launchIn(viewModelScope)
    }


    private fun setLoader(state: WaitLoader) {
        _waitLoader.value = state
    }

    private fun observeMapAction() {
        interactor.observeMapAction()
            .onEach {
                observeMapActionComplete(it)
            }
            .catch{
                    logException(it,"observeMapAction")
                }
            .launchIn(viewModelScope)

    }

    private fun observeMapActionComplete(it: CourierMapAction) {//!!!!!!!!!
        when (it) {
            is CourierMapAction.ItemClick -> {
                onMapPointClick(it.point)
            }
            is CourierMapAction.MapClick -> showManagerBar()
            is CourierMapAction.ShowAll -> onShowAllClick()
            else -> {}
        }
    }

    private fun showManagerBar() {
        interactor.mapState(CourierMapState.ShowManagerBar)
    }

    private fun onShowAllClick() {
        zoomMarkersFromBoundingBox()
    }


    private fun onMapPointClick(mapPoint: MapPoint) {
        //onTechEventLog("onItemPointClick")
        val mapPointClickId = mapPoint.id.split(" ")[0]
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
                if (item.point.id.split(" ")[0] == mapPointClickId)
                    if (isSelected) {
                        changeEnableNavigator(true)
                        getSelectedMapIcon(item.type)
                    } else {
                        changeEnableNavigator(false)
                        normalIcon
                    }
                else normalIcon
        }
        initCourierIntransit(mapPointClickId.toInt())
    }

    private fun initCourierIntransit(selectIndex: Int) {
        when(intransitItems[selectIndex]){
            is CourierIntransitEmptyItem -> {
                _courierIntransitEmptyItemLiveData.value =  intransitItems[selectIndex] as CourierIntransitEmptyItem
                _currentItemBackgroundForBottomSheet.value = IntransitItemType.Empty
            }
            is CourierIntransitCompleteItem ->{
                _courierIntransitCompleteItemLiveData.value = intransitItems[selectIndex] as CourierIntransitCompleteItem
                _currentItemBackgroundForBottomSheet.value = IntransitItemType.Complete
            }
            is CourierIntransitUndeliveredAllItem -> {
                _courierIntransitUndeliveredAllItemLiveData.value = intransitItems[selectIndex] as CourierIntransitUndeliveredAllItem
                _currentItemBackgroundForBottomSheet.value = IntransitItemType.FailedUnloadingAll
            }
            is CourierIntransitUnloadingExpectsItem -> {
                _courierIntransitUnloadingExpectsItemLiveData.value = intransitItems[selectIndex] as CourierIntransitUnloadingExpectsItem
                _currentItemBackgroundForBottomSheet.value = IntransitItemType.UnloadingExpects
            }
        }
    }

    private fun updateMarkers() {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers.toMutableSet()))
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
        //onTechEventLog("initOfficesComplete", "dstOffices count " + dstOffices.size)
        //val schedule = getSchedule()
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
                            timeWork = "пн - вс: 10:00 - 18:00",//+
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
                            timeWork = "пн - вс: 10:00 - 18:00",
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
                            timeWork = "пн - вс: 10:00 - 18:00",
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
                            timeWork = "пн - вс: 10:00 - 18:00", //+-
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                    }
                }
//
//                _currentItemBackgroundForBottomSheet.value = type

                val mapPoint = MapPoint(index.toString(), latitude, longitude,null)
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
        _intransitOrders.value =
            if (items.isEmpty()) CourierIntransitItemState.Empty
            else CourierIntransitItemState.InitItems(items, boxTotal)

    }

    private fun initMap(
        mapMarkers: MutableList<IntransitMapMarker>,
        coordinatePoints: MutableList<CoordinatePoint>
    ) {

        if (mapMarkers.isEmpty()) {
            interactor.mapState(CourierMapState.NavigateToPoint(moscowCoordinatePoint()))
        } else {
            interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers.toMutableSet()))
            val boundingBox =
                MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints.toMutableSet())
            interactor.mapState(CourierMapState.ZoomToBoundingBox(boundingBox, true))
        }

    }

    fun onNavigatorClick() {
        //onTechEventLog("Button navigate to navigator")
        selectedItem()
    }

    private fun selectedItem() {
        intransitItems.forEachIndexed { index, item ->
            if (item.isSelected) {
                val point = coordinatePoints[index]
                _navigationState.value =
                    CourierIntransitNavigationState.NavigateToNavigator(
                        point.latitude,
                        point.longitude
                    )
                return
            }
        }
    }

    fun onScanQrPvzClick() {
        updateMarkers()
        changeSelectedItemsByMarker(0, false)
        updateAndScrollToItems(0)
        _navigationState.value = CourierIntransitNavigationState.NavigateToScanner
    }

    fun onCompleteDeliveryClick() {
        viewModelScope.launch {
            try {
                _isEnableState.value = false
                val boxes = interactor.getBoxes()
                val order = interactor.getOrder()
                val srcOfficeID = interactor.getSrcOfficeID()
                val orderId = order?.orderId.toString()
                setLoader(WaitLoader.Wait)
                interactor.setIntransitTask(orderId, srcOfficeID?:0, boxes)
                interactor.completeDelivery(order!!)
                completeDeliveryComplete(order.cost)
            } catch (e: Exception) {
                logException(e,"onCompleteDeliveryClick")
                setLoader(WaitLoader.Complete)
                errorDialogManager.showErrorDialog(e, _navigateToErrorDialog)
            }
        }
    }

    private fun completeDeliveryComplete(cost: Int) {
        setLoader(WaitLoader.Complete)
        viewModelScope.launch {
            val boxes = interactor.getBoxes()
            val cdr = CompleteDeliveryResult(
                boxes.filter { box -> box.deliveredAt != "" }.size,
                boxes.size,
                cost
            )

            interactor.clearLocalTaskData()
            _navigationState.value =
                CourierIntransitNavigationState.NavigateToCompleteDelivery(
                    cdr.cost,
                    cdr.deliveredBoxes,
                    cdr.countBoxes
                )

            //clearSubscription()
        }
    }

    fun onErrorDialogConfirmClick() {
        _isEnableState.value = true
    }

    fun onItemOfficeClick(index: Int) {
        //onTechEventLog("Select Office index=$index")
        changeSelectedItems(index)
        updateItems()
        val isSelected = intransitItems[index].isSelected
        changeEnableNavigator(isSelected)
        changeSelectedMarkers(isSelected, index)
        initCourierIntransit(index)
        updateMarkers(isSelected, index)
    }


    private fun changeEnableNavigator(isSelected: Boolean) {
//        val value = sharedWorker.load(SharedWorker.ADDRESS_DETAIL_SCHEDULE_FOR_INTRANSIT, "")
//        val parts = value.split(";")
//        val scheduleOrder = parts[1]
//
//
//        val address = parts[0]
        _navigatorState.value =
            if (isSelected) {
                CourierIntransitNavigatorUIState.Enable
            }
            else {
                CourierIntransitNavigatorUIState.Disable
            }

    }

    private fun changeSelectedItems(selectIndex: Int) {
        intransitItems.forEachIndexed { index, item ->
            intransitItems[index].isSelected =
                if (selectIndex == index) {
                    !item.isSelected
                } else false
        }
        //initCourierIntransit(selectIndex)

    }

    private fun updateItems() {
        _intransitOrders.value =
            if (intransitItems.isEmpty()) CourierIntransitItemState.Empty
            else CourierIntransitItemState.UpdateItems(intransitItems)

    }

    private fun changeSelectedMarkers(isSelected: Boolean, selectIndex: Int) {
        mapMarkers.forEach { item ->
            with(item) {
                icon = if (point.id.split(" ")[0] == selectIndex.toString()) {
                    if (isSelected) getSelectedMapIcon(type) else getNormalMapIcon(type)
                } else {
                    getNormalMapIcon(type)
                }
            }
        }
    }

    private fun updateMarkers(isSelected: Boolean, selectIndex: Int) {
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers.toMutableSet()))
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

    private fun zoomMarkersFromBoundingBox() {
        val boundingBox = MapEnclosingCircle().allCoordinatePointToBoundingBox(coordinatePoints.toMutableSet())
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

