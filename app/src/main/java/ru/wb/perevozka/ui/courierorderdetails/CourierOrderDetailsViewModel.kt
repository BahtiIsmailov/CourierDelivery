package ru.wb.perevozka.ui.courierorderdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.ARRIVE_FOR
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
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

    init {
        initToolbar()
        initOrderInfo()
        initOrderItems()
    }

    private fun initToolbar() {
        _toolbarLabelState.value = Label(parameters.title)
    }

    private fun initOrderInfo() {
        with(parameters.order) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderDetailsInfoUIState.InitOrderInfo(
                resourceProvider.getOrder(id),
                resourceProvider.getCoast(coast),
                resourceProvider.getBoxCountAndVolume(minBoxesCount, minVolume),
                resourceProvider.getPvz(dstOffices.size),
                "МАРШРУТ № 4 «ПОДОЛЬСК СЕВЕР»"
            )
        }
    }

    private fun initOrderItems() {
        val items = mutableListOf<CourierOrderDetailsItem>()
        parameters.order.dstOffices.forEachIndexed { index, item ->
            items.add(CourierOrderDetailsItem(index, item.fullAddress))
        }
        _orderDetails.value = if (items.isEmpty()) CourierOrderDetailsUIState.Empty
        else CourierOrderDetailsUIState.InitItems(items)
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
    }

    fun takeOrderClick() {
        _navigationState.value = CourierOrderDetailsNavigationState.NavigateToDialogConfirm(
            resourceProvider.getConfirmDialogTitle(),
            resourceProvider.getConfirmDialogMessage(ARRIVE_FOR, parameters.order.minVolume)
        )
    }

    fun confirmTakeOrderClick() {
        _progressState.value = CourierOrderDetailsProgressState.Progress
        addSubscription(
            interactor.anchorTask(parameters.order.id.toString())
                .subscribe({
                    _progressState.value = CourierOrderDetailsProgressState.ProgressComplete
                }, {
                    _progressState.value = CourierOrderDetailsProgressState.ProgressComplete

                    // TODO: 25.08.2021 выключено до полной реализации экранов номера автомобиля
//                    _navigationState.value = CourierOrderDetailsNavigationState.NavigateToDialogInfo(
//                        DialogStyle.WARNING.ordinal,
//                        "Заказ забрали",
//                        "Этот заказ уже взят в работу",
//                        "Вернуться к списку заказов"
//                    )
                    _navigationState.value = CourierOrderDetailsNavigationState.NavigateToCarNumber(
                        parameters.title,
                        parameters.order
                    )
                })
        )
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

    data class NavigateToMessageInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    )

    data class Label(val label: String)

}