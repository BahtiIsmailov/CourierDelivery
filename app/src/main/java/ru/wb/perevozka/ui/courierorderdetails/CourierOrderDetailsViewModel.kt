package ru.wb.perevozka.ui.courierorderdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.couriermap.CourierMapMarker
import ru.wb.perevozka.ui.couriermap.CourierMapState
import ru.wb.perevozka.ui.couriermap.Empty
import ru.wb.perevozka.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.map.CoordinatePoint
import ru.wb.perevozka.utils.map.MapEnclosingCircle
import ru.wb.perevozka.utils.map.MapPoint
import java.text.DecimalFormat

class CourierOrderDetailsViewModel(
    private val parameters: CourierOrderDetailsParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderDetailsInteractor,
    private val resourceProvider: CourierOrderDetailsResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

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
        initToolbar()
    }

    fun update() {
        initOrder()
    }

    private fun initToolbar() {
        _toolbarLabelState.value = Label(parameters.title)
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeOrderData()
                .subscribe({
                    initOrderInfo(it.courierOrderLocalEntity, it.dstOffices.size)
                    initOrderItems(it.dstOffices)
                }, {})
        )
    }

    private fun initOrderInfo(courierOrderLocalEntity: CourierOrderLocalEntity, pvz: Int) {
        with(courierOrderLocalEntity) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderDetailsInfoUIState.InitOrderInfo(
                resourceProvider.getOrder(id),
                resourceProvider.getCoast(coast),
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
        interactor.mapState(CourierMapState.UpdateMapMarkers(mapMarkers))
        LogUtils { logDebugApp("coordinatePoints " + coordinatePoints.toString()) }
        val startNavigation = MapEnclosingCircle().minimumEnclosingCircle(coordinatePoints)
        LogUtils { logDebugApp("startNavigation " + startNavigation.toString()) }
        interactor.mapState(CourierMapState.ZoomAllMarkers(startNavigation))
    }

    private fun initItems(items: MutableList<CourierOrderDetailsItem>) {
        _orderDetails.value = if (items.isEmpty()) CourierOrderDetailsUIState.Empty
        else CourierOrderDetailsUIState.InitItems(items)
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
    }

    fun takeOrderClick() {
        _navigationState.value = if (interactor.carNumberIsConfirm()) {
            CourierOrderDetailsNavigationState.NavigateToOrderConfirm
        } else {
            CourierOrderDetailsNavigationState.NavigateToCarNumber(
                parameters.title,
                parameters.order
            )
        }
//        _navigationState.value = CourierOrderDetailsNavigationState.NavigateToDialogConfirm(
//            resourceProvider.getConfirmDialogTitle(),
//            resourceProvider.getConfirmDialogMessage(ARRIVE_FOR_COURIER_MIN, parameters.order.minVolume)
//        )
    }

    // TODO: 31.08.2021 реализовать далее по flow
//    fun confirmTakeOrderClick()
//   {
//        _progressState.value = CourierOrderDetailsProgressState.Progress
//        addSubscription(
//            interactor.anchorTask(parameters.order.id.toString())
//                .subscribe({
//                    _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
//                }, {
//                    _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
//
//                    // TODO: 25.08.2021 выключено до полной реализации экранов номера автомобиля
////                    _navigationState.value = CourierOrderDetailsNavigationState.NavigateToDialogInfo(
////                        DialogStyle.WARNING.ordinal,
////                        "Заказ забрали",
////                        "Этот заказ уже взят в работу",
////                        "Вернуться к списку заказов"
////                    )
//                    _navigationState.value = CourierOrderDetailsNavigationState.NavigateToCarNumber(
//                        parameters.title,
//                        parameters.order
//                    )
//                })
//        )
//    }

    fun confirmTakeOrderClick() {

    }

    private fun courierWarehouseError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> NavigateToMessageInfo(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToMessageInfo(
                DialogStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToMessageInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
        progressComplete()
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    fun onItemClick(index: Int) {
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
        interactor.mapState(CourierMapState.UpdateMapMarkers(mapMarkers))
        interactor.mapState(CourierMapState.NavigateToMarker(selectIndex.toString()))
    }

    data class NavigateToMessageInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    )

    data class Label(val label: String)

}