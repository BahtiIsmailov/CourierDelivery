package ru.wb.perevozka.ui.courierintransit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierintransit.delegates.items.BaseIntransitItem
import ru.wb.perevozka.ui.courierintransit.delegates.items.CourierIntransitCompleteItem
import ru.wb.perevozka.ui.courierintransit.delegates.items.CourierIntransitEmptyItem
import ru.wb.perevozka.ui.courierintransit.delegates.items.CourierIntransitFailedItem
import ru.wb.perevozka.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.map.CoordinatePoint
import ru.wb.perevozka.utils.map.MapCircle
import ru.wb.perevozka.utils.map.MapEnclosingCircle
import ru.wb.perevozka.utils.map.MapPoint
import ru.wb.perevozka.utils.time.DateTimeFormatter

class CourierIntransitViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierIntransitInteractor,
    private val resourceProvider: CourierIntransitResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _orderDetails = MutableLiveData<CourierIntransitItemState>()
    val orderDetails: LiveData<CourierIntransitItemState>
        get() = _orderDetails

    private val _mapPoint = MutableLiveData<CourierIntransitMapPoint>()
    val mapPoint: LiveData<CourierIntransitMapPoint>
        get() = _mapPoint

    private val _navigationState = SingleLiveEvent<CourierIntransitNavigationState>()
    val navigationState: LiveData<CourierIntransitNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierIntransitProgressState>()
    val progressState: LiveData<CourierIntransitProgressState>
        get() = _progressState

    private val _intransitTime = MutableLiveData<CourierIntransitTimeState>()
    val intransitTime: LiveData<CourierIntransitTimeState>
        get() = _intransitTime

    private var copyIntransitItems = mutableListOf<BaseIntransitItem>()

    private var mapPointItems = mutableListOf<CourierIntransitMapPointItem>()

    private fun copyItems(items: List<BaseIntransitItem>) {
        copyIntransitItems = items.toMutableList()
    }

    private fun copyMapPoints(items: List<CourierIntransitMapPointItem>) {
        mapPointItems = items.toMutableList()
    }

    init {
        initToolbar()
        initOffices()
        initTime()
        initScanner()
    }

    private fun initScanner() {
        addSubscription(interactor.observeOfficeIdScanProcess().subscribe({
            _navigationState.value = CourierIntransitNavigationState.NavigateToUnloadingScanner(it)
        }, {}))
    }

    private fun initTime() {
        addSubscription(interactor.startTime().subscribe({
            _intransitTime.value = CourierIntransitTimeState.Time(
                DateTimeFormatter.getDigitFullTime(it.toInt())
            )
        }, {}))
    }

    private fun initToolbar() {
        _toolbarLabelState.value = Label(resourceProvider.getLabel())
    }

    private fun initOffices() {
        addSubscription(
            interactor.observeBoxesGroupByOffice()
                .subscribe({ initOfficesComplete(it) }, { initOfficesError(it) })
        )
    }

    private fun initOfficesError(it: Throwable) {
        LogUtils { logDebugApp("initOrderItemsError " + it) }
    }

    private fun initOfficesComplete(dstOffices: List<CourierIntransitGroupByOfficeEntity>) {
        val items = mutableListOf<BaseIntransitItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val mapPointItems = mutableListOf<CourierIntransitMapPointItem>()
        var deliveredCountTotal = 0
        var fromCountTotal = 0
        dstOffices.forEachIndexed { index, item ->
            with(item) {
                deliveredCountTotal += deliveredCount
                fromCountTotal += fromCount
                val intransitItem: BaseIntransitItem
                val mapPointItem: CourierIntransitMapPointItem
                if (deliveredCount == 0) {
                    intransitItem = CourierIntransitEmptyItem(
                        id = index,
                        fullAddress = address,
                        deliveryCount = deliveredCount.toString(),
                        fromCount = fromCount.toString(),
                        isSelected = DEFAULT_SELECT_ITEM,
                        idView = index
                    )
                    mapPointItem = Empty(
                        MapPoint(index.toString(), latitude, longitude),
                        resourceProvider.getEmptyMapIcon()
                    )
                } else {
                    if (deliveredCount == fromCount) {
                        intransitItem = CourierIntransitCompleteItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredCount.toString(),
                            fromCount = fromCount.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                        mapPointItem = Complete(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getCompleteMapIcon()
                        )
                    } else {
                        intransitItem = CourierIntransitFailedItem(
                            id = index,
                            fullAddress = address,
                            deliveryCount = deliveredCount.toString(),
                            fromCount = fromCount.toString(),
                            isSelected = DEFAULT_SELECT_ITEM,
                            idView = index
                        )
                        mapPointItem = Failed(
                            MapPoint(index.toString(), latitude, longitude),
                            resourceProvider.getFailedMapIcon()
                        )
                    }
                }
                items.add(intransitItem)
                coordinatePoints.add(CoordinatePoint(latitude, longitude))
                mapPointItems.add(mapPointItem)
            }
        }
        copyItems(items)
        val boxTotalCount =
            resourceProvider.getBoxCountAndTotal(deliveredCountTotal, fromCountTotal)
        initItems(items, boxTotalCount)

        val startNavigation = MapEnclosingCircle().minimumEnclosingCircle(coordinatePoints)
        copyMapPoints(mapPointItems)
        initMap(mapPointItems, startNavigation)

        if (deliveredCountTotal == fromCountTotal)
            _orderDetails.value = CourierIntransitItemState.CompleteDelivery
    }

    private fun initItems(items: MutableList<BaseIntransitItem>, boxTotal: String) {
        _orderDetails.value = if (items.isEmpty()) CourierIntransitItemState.Empty
        else CourierIntransitItemState.InitItems(items, boxTotal)
    }

    private fun initMap(
        mapPoints: MutableList<CourierIntransitMapPointItem>,
        coordinatePoints: MapCircle
    ) {
        if (mapPoints.isEmpty()) {
            _mapPoint.value = CourierIntransitMapPoint.NavigateToPoint(moscowMapPoint())
        } else {
            _mapPoint.value = CourierIntransitMapPoint.InitMapPoint(mapPoints, coordinatePoints)
        }
    }

    private fun moscowMapPoint() = MapPoint("0", 55.751244, 37.618423)

    fun scanQrPvzClick() {
        _navigationState.value = CourierIntransitNavigationState.NavigateToScanner
    }

    fun completeDeliveryClick() {
        _progressState.value = CourierIntransitProgressState.Progress
        addSubscription(interactor.completeDelivery()
            .subscribe({ completeDeliveryComplete() }, { completeDeliveryError(it) })
        )
    }

    private fun completeDeliveryComplete() {
        _progressState.value = CourierIntransitProgressState.ProgressComplete
        _navigationState.value = CourierIntransitNavigationState.NavigateToCompleteDelivery
    }

    private fun completeDeliveryError(throwable: Throwable) {
        LogUtils { logDebugApp(throwable.toString()) }
        _progressState.value = CourierIntransitProgressState.ProgressComplete
        val message = when (throwable) {
            is NoInternetException -> CourierIntransitNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                "Интернет-соединение отсутствует",
                throwable.message,
                "Понятно"
            )
            is BadRequestException -> CourierIntransitNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> CourierIntransitNavigationState.NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        _navigationState.value = message
    }

    fun closeScannerClick() {
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
        mapPointItems.forEach { item ->
            val icon = if (item.point.id == selectIndex.toString()) {
                if (isSelected) getSelectedIcon(item) else getNormalIcon(item)
            } else {
                getNormalIcon(item)
            }
            item.icon = icon
        }
        mapPointItems.find { it.point.id == selectIndex.toString() }?.apply {
            mapPointItems.remove(this)
            mapPointItems.add(this)
        }

        _mapPoint.value = CourierIntransitMapPoint.UpdateMapPoints(mapPointItems)
        _mapPoint.value = CourierIntransitMapPoint.NavigateToPointById(selectIndex.toString())
    }

    private fun getNormalIcon(item: CourierIntransitMapPointItem) =
        when (item) {
            is Empty -> resourceProvider.getEmptyMapIcon()
            is Failed -> resourceProvider.getFailedMapIcon()
            is Complete -> resourceProvider.getCompleteMapIcon()
            else -> resourceProvider.getEmptyMapIcon()
        }

    private fun getSelectedIcon(item: CourierIntransitMapPointItem) =
        when (item) {
            is Empty -> resourceProvider.getEmptyMapSelectedIcon()
            is Failed -> resourceProvider.getFailedMapSelectedIcon()
            is Complete -> resourceProvider.getCompleteMapSelectIcon()
            else -> resourceProvider.getEmptyMapSelectedIcon()
        }

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    data class NavigateToMessageInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    )

    data class Label(val label: String)

    companion object {
        const val DEFAULT_SELECT_ITEM = false
    }

}