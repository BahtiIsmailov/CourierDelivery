package ru.wb.go.ui.courierintransit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransit.delegates.items.*
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.go.ui.courierintransit.domain.CourierIntransitScanOfficeData
import ru.wb.go.ui.couriermap.*
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapEnclosingCircle
import ru.wb.go.utils.map.MapPoint
import ru.wb.go.utils.time.DateTimeFormatter
import java.util.concurrent.TimeUnit

class CourierIntransitViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierIntransitInteractor,
    private val resourceProvider: CourierIntransitResourceProvider,
    private val userManager: UserManager,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigateToInformation = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToInformation: LiveData<NavigateToDialogInfo>
        get() = _navigateToInformation

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

    private val _progressState = MutableLiveData<CourierIntransitProgressState>()
    val progressState: LiveData<CourierIntransitProgressState>
        get() = _progressState

    private val _intransitTime = MutableLiveData<CourierIntransitTimeState>()
    val intransitTime: LiveData<CourierIntransitTimeState>
        get() = _intransitTime

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
        observeBoxesGroupByOffice()
        initTime()
        initScanner()
        observeMapAction()
    }

    private fun initToolbar() {
        addSubscription(
            interactor.taskId().subscribe(
                { _toolbarLabelState.value = Label(resourceProvider.getLabelId(it)) },
                { _toolbarLabelState.value = Label(resourceProvider.getLabel()) })
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun observeBoxesGroupByOffice() {
        addSubscription(
            interactor.observeBoxesGroupByOffice()
                .map { offices ->
                    offices.toMutableList().sortedWith(
                        compareBy({ it.isUnloaded }, { it.deliveredCount == it.fromCount })
                    )
                }
                .subscribe({ initOfficesComplete(it) }, { initOfficesError(it) })
        )
    }

    private fun initTime() {
        addSubscription(interactor.startTime().subscribe({
            _intransitTime.value = CourierIntransitTimeState.Time(
                DateTimeFormatter.getDigitFullTime(it.toInt())
            )
        }, {}))
    }

    private fun initScanner() {
        addSubscription(interactor.observeOfficeIdScanProcess()
            .retryWhen { errorObservable -> errorObservable.delay(1, TimeUnit.SECONDS) }
            .subscribe({
                when (it) {
                    is CourierIntransitScanOfficeData.Office -> {
                        _beepEvent.value = CourierIntransitScanOfficeBeepState.Office
                        _navigationState.value =
                            CourierIntransitNavigationState.NavigateToUnloadingScanner(it.id)
                        onCleared()
                    }
                    CourierIntransitScanOfficeData.UnknownOffice -> {
                        _beepEvent.value = CourierIntransitScanOfficeBeepState.UnknownOffice
                    }
                }
            }, {
                LogUtils { logDebugApp("initScanner error office scan " + it) }
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
                        LogUtils { logDebugApp("CourierMapAction.PermissionComplete getWarehouse()") }
                        observeBoxesGroupByOffice()
                    }
                    is CourierMapAction.AutomatedLocationUpdate -> {
                    }
                    is CourierMapAction.ForcedLocationUpdate -> {
                    }
                }
            },
                {}
            ))
    }

    private fun initOfficesError(it: Throwable) {
        LogUtils { logDebugApp("initOrderItemsError " + it) }
    }

    private fun initOfficesComplete(dstOffices: List<CourierIntransitGroupByOfficeEntity>) {

        LogUtils { logDebugApp("initOfficesComplete ===================================================== ") }
        val items = mutableListOf<BaseIntransitItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val markers = mutableListOf<CourierMapMarker>()

        var deliveredCountTotal = 0
        var fromCountTotal = 0
        dstOffices.forEachIndexed { index, item ->
            with(item) {
                deliveredCountTotal += deliveredCount
                fromCountTotal += fromCount
                val intransitItem: BaseIntransitItem
                val mapMarker: CourierMapMarker
                if (deliveredCount == 0 && visitedAt.isEmpty()) {
                    intransitItem = CourierIntransitEmptyItem(
                        id = index,
                        fullAddress = address,
                        deliveryCount = deliveredCount.toString(),
                        fromCount = fromCount.toString(),
                        isSelected = DEFAULT_SELECT_ITEM,
                        idView = index
                    )
                    mapMarker = Empty(
                        MapPoint(index.toString(), latitude, longitude),
                        resourceProvider.getEmptyMapIcon()
                    )
                } else {
                    if (deliveredCount == fromCount) {
                        if (isUnloaded) {
                            intransitItem = CourierIntransitCompleteItem(
                                id = index,
                                fullAddress = address,
                                deliveryCount = deliveredCount.toString(),
                                fromCount = fromCount.toString(),
                                isSelected = DEFAULT_SELECT_ITEM,
                                idView = index
                            )

                            mapMarker = Complete(
                                MapPoint(index.toString(), latitude, longitude),
                                resourceProvider.getCompleteMapIcon()
                            )

                        } else {
                            intransitItem = CourierIntransitUnloadingExpectsItem(
                                id = index,
                                fullAddress = address,
                                deliveryCount = deliveredCount.toString(),
                                fromCount = fromCount.toString(),
                                isSelected = DEFAULT_SELECT_ITEM,
                                idView = index
                            )

                            mapMarker = Wait(
                                MapPoint(index.toString(), latitude, longitude),
                                resourceProvider.getWaitMapIcon()
                            )
                        }

                    } else {
                        intransitItem = if (isUnloaded) {
                            CourierIntransitFailedUnloadingAllItem(
                                id = index,
                                fullAddress = address,
                                deliveryCount = deliveredCount.toString(),
                                fromCount = fromCount.toString(),
                                isSelected = DEFAULT_SELECT_ITEM,
                                idView = index
                            )
                        } else {
                            CourierIntransitFaledUnloadingExpectsItem(
                                id = index,
                                fullAddress = address,
                                deliveryCount = deliveredCount.toString(),
                                fromCount = fromCount.toString(),
                                isSelected = DEFAULT_SELECT_ITEM,
                                idView = index
                            )
                        }

                        mapMarker = Failed(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getFailedMapIcon()
                        )
                    }
                }

                items.add(intransitItem)
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
            interactor.mapState(CourierMapState.NavigateToPoint(moscowMapPoint()))
        } else {
            interactor.mapState(CourierMapState.UpdateMarkers(mapMarkers))
            LogUtils { logDebugApp("coordinatePoints " + coordinatePoints.toString()) }
            val boundingBox = MapEnclosingCircle().minimumBoundingBox(coordinatePoints)
            interactor.mapState(CourierMapState.ZoomToCenterBoundingBox(boundingBox))
        }
    }

    fun onScanQrPvzClick() {
        onStartScanner()
        _navigationState.value = CourierIntransitNavigationState.NavigateToScanner
    }

    fun onCompleteDeliveryClick() {
        _progressState.value = CourierIntransitProgressState.Progress
        addSubscription(
            interactor.completeDelivery()
                .subscribe({ completeDeliveryComplete(it) }, { completeDeliveryError(it) })
        )
    }

    private fun completeDeliveryComplete(completeDeliveryResult: CompleteDeliveryResult) {
        _progressState.value = CourierIntransitProgressState.ProgressComplete
        _navigationState.value = CourierIntransitNavigationState.NavigateToCompleteDelivery(
            userManager.costTask(),
            completeDeliveryResult.unloadedCount,
            completeDeliveryResult.fromCount
        )
    }

    private fun completeDeliveryError(throwable: Throwable) {
        LogUtils { logDebugApp(throwable.toString()) }
        _progressState.value = CourierIntransitProgressState.ProgressComplete
        val message = when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                "Интернет-соединение отсутствует",
                throwable.message,
                "Понятно"
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigateToInformation.value = message
    }

    fun onCloseScannerClick() {
        onStopScanner()
        _navigationState.value = CourierIntransitNavigationState.NavigateToMap
    }

    fun confirmTakeOrderClick() {

    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    fun onItemClick(index: Int) {
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
        interactor.scannerAction(ScannerState.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    data class Label(val label: String)

    companion object {
        const val DEFAULT_SELECT_ITEM = false
    }

}