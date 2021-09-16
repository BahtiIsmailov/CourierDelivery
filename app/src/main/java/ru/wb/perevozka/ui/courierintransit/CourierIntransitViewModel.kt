package ru.wb.perevozka.ui.courierintransit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierintransit.domain.CourierIntransitInteractor
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.map.CoordinatePoint
import ru.wb.perevozka.utils.map.MapEnclosingCircle
import ru.wb.perevozka.utils.map.MapPoint

class CourierIntransitViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierIntransitInteractor,
    private val resourceProvider: CourierIntransitResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _orderDetails = MutableLiveData<CourierIntransitUIState>()
    val orderDetails: LiveData<CourierIntransitUIState>
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

    private var copyCourierIntransitItems = mutableListOf<CourierIntransitItem>()

    private fun copyCourierOrderDetailsItems(items: List<CourierIntransitItem>) {
        copyCourierIntransitItems = items.toMutableList()
    }

    init {
        initToolbar()
        initOrder()
    }

    private fun initToolbar() {
        _toolbarLabelState.value = Label(resourceProvider.getLabel())
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeBoxesGroupByOrder()
                .subscribe({ initOrderItemsComplete(it) }, { initOrderItemsError(it) })
        )
    }

    private fun initOrderItemsError(it: Throwable) {
        LogUtils { logDebugApp("initOrderItemsError " + it) }
    }

    private fun initOrderItemsComplete(dstOffices: List<CourierIntransitGroupByOfficeEntity>) {
        val items = mutableListOf<CourierIntransitItem>()
        val coordinatePoints = mutableListOf<CoordinatePoint>()
        val mapPoints = mutableListOf<MapPoint>()
        dstOffices.forEachIndexed { index, item ->
            with(item) {
                val countBox = resourceProvider.getBoxCountAndTotal(deliveredCount, fromCount)
                val intransitItem = CourierIntransitItem(
                    index,
                    address,
                    countBox,
                    DEFAULT_SELECT_ITEM
                )
                items.add(intransitItem)
                coordinatePoints.add(CoordinatePoint(latitude, longitude))
                mapPoints.add(MapPoint(index.toString(), latitude, longitude))
            }
        }
        copyCourierOrderDetailsItems(items)
        initItems(items)
        initMap(coordinatePoints, mapPoints)
    }

    private fun initMap(
        coordinatePoints: MutableList<CoordinatePoint>,
        mapPoints: MutableList<MapPoint>
    ) {
        val startNavigation = MapEnclosingCircle().minimumEnclosingCircle(coordinatePoints)
        _mapPoint.value = CourierIntransitMapPoint.InitMapPoint(mapPoints, startNavigation)
    }

    private fun initItems(items: MutableList<CourierIntransitItem>) {
        _orderDetails.value = if (items.isEmpty()) CourierIntransitUIState.Empty
        else CourierIntransitUIState.InitItems(items)
    }

    private fun progressComplete() {
        _progressState.value = CourierIntransitProgressState.ProgressComplete
    }

    fun scanQrPvzClick() {

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
        copyCourierIntransitItems.forEachIndexed { index, item ->
            val copySelectedItem = if (selectIndex == index) {
                copyCourierIntransitItems[index].copy(isSelected = !item.isSelected)
            } else {
                copyCourierIntransitItems[index].copy(isSelected = false)
            }
            copyCourierIntransitItems[index] = copySelectedItem
        }
        initItems(copyCourierIntransitItems)
        navigateToPoint(selectIndex, copyCourierIntransitItems[selectIndex].isSelected)
    }

    private fun navigateToPoint(index: Int, isSelected: Boolean) {
        val itemSelected = copyCourierIntransitItems[index]
        _mapPoint.value =
            CourierIntransitMapPoint.NavigateToPoint(itemSelected.id.toString(), isSelected)
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