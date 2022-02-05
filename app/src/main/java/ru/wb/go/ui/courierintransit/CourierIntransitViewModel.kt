package ru.wb.go.ui.courierintransit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.exceptions.CustomException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransit.delegates.items.*
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.go.ui.courierintransit.domain.CourierIntransitScanOfficeData
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
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
    private val deviceManager: DeviceManager,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
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

    private val _navigateToErrorDialog = SingleLiveEvent<ErrorDialogData>()
    val navigateToErrorDialog: LiveData<ErrorDialogData>
        get() = _navigateToErrorDialog

    private val _navigateToDialogConfirmInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmInfo

    private val _orderDetails = MutableLiveData<CourierIntransitItemState>()
    val orderDetails: LiveData<CourierIntransitItemState>
        get() = _orderDetails

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

    private var copyIntransitItems = mutableListOf<BaseIntransitItem>()

    private var mapMarkers = mutableListOf<CourierMapMarker>()

    private fun copyItems(items: List<BaseIntransitItem>) {
        copyIntransitItems = items.toMutableList()
    }

    private fun copyMapPoints(items: List<CourierMapMarker>) {
        mapMarkers = items.toMutableList()
    }

    init {
        initToolbar()
        observeNetworkState()
        fetchVersionApp()
        observeOffices()
        initTime()
        initScanner()
        observeMapAction()
    }

    private fun initToolbar() {
        addSubscription(
            interactor.getOrderId()
                .subscribe(
                    { _toolbarLabelState.value = Label(resourceProvider.getLabelId(it)) },
                    { _toolbarLabelState.value = Label(resourceProvider.getLabel()) })
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.toolbarVersion)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun observeOffices() {
        addSubscription(
            interactor.getOffices()
                .subscribe({ initOfficesComplete(it) }, { initOfficesError(it) })
        )
    }

    private fun initTime() {
        addSubscription(
            interactor.initOrderTimer()
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

    private fun initScanner() {
        addSubscription(
            interactor.observeOfficeIdScanProcess()
                .subscribe({
                    when (it) {
                        is CourierIntransitScanOfficeData.NecessaryOffice -> {
                            _beepEvent.value = CourierIntransitScanOfficeBeepState.Office
                            _navigationState.value =
                                CourierIntransitNavigationState.NavigateToUnloadingScanner(it.id)
                            onCleared()
                        }
                        CourierIntransitScanOfficeData.UnknownQrOffice -> {
                            val ex = CustomException("QR код офиса не распознан")
                            _beepEvent.value = CourierIntransitScanOfficeBeepState.UnknownQrOffice
                            errorDialogManager.showErrorDialog(ex, _navigateToErrorDialog)
                        }
                        CourierIntransitScanOfficeData.WrongOffice -> {
                            val ex = CustomException("Офис не принадлежит маршруту")
                            _beepEvent.value = CourierIntransitScanOfficeBeepState.WrongOffice
                            errorDialogManager.showErrorDialog(ex, _navigateToErrorDialog)
                        }
                    }
                }, {
                    onTechErrorLog("observeOfficeIdScanProcess", it)
                    errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
                })
        )
    }

    private fun observeMapAction() {
        addSubscription(
            interactor.observeMapAction().subscribe({
                when (it) {
                    is CourierMapAction.ItemClick -> {
                    }
                    CourierMapAction.PermissionComplete -> {
                        onTechEventLog("observeMapAction", "PermissionComplete")
                        observeOffices()
                    }
                    is CourierMapAction.AutomatedLocationUpdate -> {
                    }
                    is CourierMapAction.ForcedLocationUpdate -> {
                    }
                }
            },
                { onTechErrorLog("observeMapAction", it) }
            ))
    }

    private fun initOfficesError(it: Throwable) {
        onTechErrorLog("initOfficesError", it)
    }

    private fun initOfficesComplete(dstOffices: List<LocalOfficeEntity>) {
        onTechEventLog("initOfficesComplete", "dstOffices count " + dstOffices.size)
        val items = mutableListOf<BaseIntransitItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val markers = mutableListOf<CourierMapMarker>()

        var deliveredCountTotal = 0
        var fromCountTotal = 0
        dstOffices.forEachIndexed { index, office ->
            with(office) {
                deliveredCountTotal += deliveredBoxes
                fromCountTotal += countBoxes
                val item: BaseIntransitItem
                val mapMarker: CourierMapMarker
                when {
                    deliveredBoxes == 0 && !isVisited -> {
                        item = CourierIntransitEmptyItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index,
                        )
                        mapMarker = Empty(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getEmptyMapIcon()
                        )
                    }
                    deliveredBoxes == countBoxes && isOnline -> {
                        item = CourierIntransitCompleteItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )

                        mapMarker = Complete(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getCompleteMapIcon()
                        )
                    }
                    deliveredBoxes == countBoxes && !isOnline -> {
                        item = CourierIntransitUnloadingExpectsItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )

                        mapMarker = Wait(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getWaitMapIcon()
                        )
                    }
                    isOnline -> {
                        item = CourierIntransitFailedUnloadingAllItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                        mapMarker = Failed(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getFailedMapIcon()
                        )
                    }
                    else -> {
                        item = CourierIntransitFaledUnloadingExpectsItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredBoxes.toString(),
                            fromCount = countBoxes.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                        mapMarker = Failed(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getFailedMapIcon()
                        )
                    }
                }

                items.add(item)
                coordinatePoints.add(CoordinatePoint(latitude, longitude))
                markers.add(mapMarker)
            }
        }
        copyItems(items)
        val boxTotalCount =
            resourceProvider.getBoxCountAndTotal(deliveredCountTotal, fromCountTotal)
        initItems(items, boxTotalCount)

        copyMapPoints(markers)
        initMap(markers, coordinatePoints)

        if (deliveredCountTotal == fromCountTotal)
            _orderDetails.value = CourierIntransitItemState.CompleteDelivery
    }

    private fun initItems(items: MutableList<BaseIntransitItem>, boxTotal: String) {
        _orderDetails.value = if (items.isEmpty()) CourierIntransitItemState.Empty
        else CourierIntransitItemState.InitItems(items, boxTotal)
    }

    private fun initMap(
        mapMarkers: MutableList<CourierMapMarker>,
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
        onTechEventLog("onScanQrPvzClick")
        onStartScanner()
        _navigationState.value = CourierIntransitNavigationState.NavigateToScanner
    }

    fun onCompleteDeliveryClick() {
        onTechEventLog("Click button CompleteDelivery")
        _isEnableState.value = false
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.completeDelivery()
                .subscribe(
                    {
                        completeDeliveryComplete(it)
                    },
                    {
                        onTechErrorLog("CompleteDelivery", it)
                        setLoader(WaitLoader.Complete)
                        errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
                    })
        )
    }

    private fun completeDeliveryComplete(cdr: CompleteDeliveryResult) {
        onTechEventLog(
            "CompleteDelivery",
            "boxes: ${cdr.deliveredBoxes}. Cost: ${cdr.cost}"
        )
        setLoader(WaitLoader.Complete)
        val ob = interactor.getOfflineBoxes()
        if (ob > 0) {
            val ex = CustomException("Ошибка передачи данных. $ob")
            onTechErrorLog("completeDelivery", ex)
            errorDialogManager.showErrorDialog(ex, _navigateToErrorDialog)
            return
        }
        interactor.clearLocalTaskData()
        _navigationState.value = CourierIntransitNavigationState.NavigateToCompleteDelivery(
            cdr.cost,
            cdr.deliveredBoxes,
            cdr.countBoxes
        )
    }

    fun onCloseScannerClick() {
        onTechEventLog("onCloseScannerClick")
        onStopScanner()
        _navigationState.value = CourierIntransitNavigationState.NavigateToMap
    }

    fun confirmTakeOrderClick() {

    }

    fun onErrorDialogConfirmClick() {
        onStartScanner()
        _isEnableState.value = true
    }

    fun onCancelLoadClick() {
        onTechEventLog("onCancelLoadClick")
        clearSubscription()
    }

    fun onItemOfficeClick(index: Int) {
        onTechEventLog("onItemOfficeClick")
        changeItemSelected(index)
    }

    private fun changeItemSelected(selectIndex: Int) {
        copyIntransitItems.forEachIndexed { index, item ->
            copyIntransitItems[index].isSelected =
                if (selectIndex == index) !item.isSelected
                else false
        }
        _orderDetails.value =
            if (copyIntransitItems.isEmpty()) CourierIntransitItemState.Empty
            else CourierIntransitItemState.UpdateItems(copyIntransitItems, selectIndex)
        navigateToPoint(selectIndex, copyIntransitItems[selectIndex].isSelected)
    }

    private fun navigateToPoint(selectIndex: Int, isSelected: Boolean) {
        mapMarkers.forEach { item ->
            val icon = if (item.point.id == selectIndex.toString()) {
                if (isSelected) getSelectedIcon(item) else getNormalIcon(item)
            } else {
                getNormalIcon(item)
            }
            item.icon = icon
        }
        mapMarkers.find { it.point.id == selectIndex.toString() }?.apply {
            mapMarkers.remove(this)
            mapMarkers.add(this)
        }
        interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
        interactor.mapState(CourierMapState.NavigateToMarker(selectIndex.toString()))
    }

    private fun getNormalIcon(mapMarker: CourierMapMarker) =
        when (mapMarker) {
            is Empty -> resourceProvider.getEmptyMapIcon()
            is Failed -> resourceProvider.getFailedMapIcon()
            is Complete -> resourceProvider.getCompleteMapIcon()
            is Wait -> resourceProvider.getWaitMapIcon()
            else -> resourceProvider.getEmptyMapIcon()
        }

    private fun getSelectedIcon(mapMarker: CourierMapMarker) =
        when (mapMarker) {
            is Empty -> resourceProvider.getEmptyMapSelectedIcon()
            is Failed -> resourceProvider.getFailedMapSelectedIcon()
            is Complete -> resourceProvider.getCompleteMapSelectIcon()
            is Wait -> resourceProvider.getWaitMapSelectedIcon()
            else -> resourceProvider.getEmptyMapSelectedIcon()
        }

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.StopScan)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    data class Label(val label: String)

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val DEFAULT_SELECT_ITEM = false
        const val SCREEN_TAG = "CourierIntransit"
    }

}