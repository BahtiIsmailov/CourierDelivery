package ru.wb.perevozka.ui.courierdelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierorderdetails.CourierOrderDetailsItem
import ru.wb.perevozka.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import java.text.DecimalFormat

class CourierDeliveryViewModel(
    private val parameters: CourierOrderDetailsParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderDetailsInteractor,
    private val resourceProvider: CourierDeliveryResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _orderInfo = MutableLiveData<CourierDeliveryInfoUIState>()
    val orderInfo: LiveData<CourierDeliveryInfoUIState>
        get() = _orderInfo

    private val _orderDetails = MutableLiveData<CourierDeliveryUIState>()
    val orderDetails: LiveData<CourierDeliveryUIState>
        get() = _orderDetails

    private val _mapPoint = MutableLiveData<CourierDeliveryMapPoint>()
    val mapPoint: LiveData<CourierDeliveryMapPoint>
        get() = _mapPoint

    private val _navigationState = SingleLiveEvent<CourierDeliveryNavigationState>()
    val navigationState: LiveData<CourierDeliveryNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierDeliveryProgressState>()
    val progressState: LiveData<CourierDeliveryProgressState>
        get() = _progressState

    private var copyCourierOrderDetailsItems = mutableListOf<CourierOrderDetailsItem>()

    private fun copyCourierOrderDetailsItems(items: List<CourierOrderDetailsItem>) {
        copyCourierOrderDetailsItems = items.toMutableList()
    }

    init {
        initToolbar()
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
            _orderInfo.value = CourierDeliveryInfoUIState.InitOrderInfo(
                resourceProvider.getOrder(id),
                resourceProvider.getCoast(coast),
                resourceProvider.getBoxCountAndVolume(minBoxesCount, minVolume),
                resourceProvider.getPvz(pvz)
            )
        }
    }

    private fun initOrderItems(dstOffices: List<CourierOrderDstOfficeLocalEntity>) {
        val items = mutableListOf<CourierOrderDetailsItem>()
        dstOffices.forEachIndexed { index, item ->
            items.add(
                CourierOrderDetailsItem(
                    index,
                    item.fullAddress,
                    item.longitude,
                    item.latitude,
                    false
                )
            )
        }
        copyCourierOrderDetailsItems(items)
        _orderDetails.value = if (items.isEmpty()) CourierDeliveryUIState.Empty
        else CourierDeliveryUIState.InitItems(items)
    }

    private fun progressComplete() {
        _progressState.value = CourierDeliveryProgressState.ProgressComplete
    }

    fun takeOrderClick() {
        _navigationState.value = if (interactor.carNumberIsConfirm()) {
            CourierDeliveryNavigationState.NavigateToOrderConfirm
        } else {
            CourierDeliveryNavigationState.NavigateToCarNumber(
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
        navigateToPoint(index)
    }

    private fun changeItemSelected(selectIndex: Int) {
        copyCourierOrderDetailsItems.forEachIndexed { index, item ->
            val copyReception = if (selectIndex == index) {
                copyCourierOrderDetailsItems[index].copy(isSelected = !item.isSelected)
            } else {
                copyCourierOrderDetailsItems[index].copy(isSelected = false)
            }
            copyCourierOrderDetailsItems[index] = copyReception
        }
        _orderDetails.value =
            if (copyCourierOrderDetailsItems.isEmpty()) CourierDeliveryUIState.Empty
            else CourierDeliveryUIState.InitItems(copyCourierOrderDetailsItems)
    }

    private fun navigateToPoint(index: Int) {
        val itemSelected = copyCourierOrderDetailsItems[index]
        _mapPoint.value =
            CourierDeliveryMapPoint.NavigateToPoint(itemSelected.lat, itemSelected.long)
    }

    data class NavigateToMessageInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    )

    data class Label(val label: String)

}