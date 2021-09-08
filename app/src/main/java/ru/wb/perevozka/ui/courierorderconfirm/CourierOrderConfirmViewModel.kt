package ru.wb.perevozka.ui.courierorderconfirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.ARRIVE_FOR_COURIER_MIN
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierorderconfirm.domain.CourierOrderConfirmInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import java.text.DecimalFormat

class CourierOrderConfirmViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderConfirmInteractor,
    private val resourceProvider: CourierOrderConfirmResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _orderInfo = MutableLiveData<CourierOrderConfirmInfoUIState>()
    val orderInfo: LiveData<CourierOrderConfirmInfoUIState>
        get() = _orderInfo

    private val _navigationState = SingleLiveEvent<CourierOrderConfirmNavigationState>()
    val navigationState: LiveData<CourierOrderConfirmNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierOrderConfirmProgressState>()
    val progressState: LiveData<CourierOrderConfirmProgressState>
        get() = _progressState


    init {
        initOrder()
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeOrderData()
                .subscribe({ initOrderInfo(it.courierOrderLocalEntity) }, {})
        )
    }

    private fun initOrderInfo(courierOrderLocalEntity: CourierOrderLocalEntity) {
        with(courierOrderLocalEntity) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderConfirmInfoUIState.InitOrderInfo(
                order = resourceProvider.getOrder(id),
                coast = resourceProvider.getCoast(coast),
                carNumber = resourceProvider.getCarNumber(interactor.carNumber()),
                arrive = resourceProvider.getArrive(ARRIVE_FOR_COURIER_MIN),
                volume = resourceProvider.getVolume(minVolume)
            )
        }
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderConfirmProgressState.ProgressComplete
    }

    fun refuseOrderClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToRefuseOrderDialog(
            "Отказаться от заказа",
            "Вы уверены, что хотите отказаться от заказа?"
        )
    }

    fun confirmOrderClick() {
        val taskId = interactor.observeOrderData().map { it.courierOrderLocalEntity.id }
            .map { it.toString() }
        addSubscription(taskId.flatMapSingle { interactor.anchorTask(it) }
            .subscribe(
                { carNumberIsConfirm() },
                { carNumberNotConfirmed() })
        )
    }

    private fun carNumberIsConfirm() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToTimer
    }

    private fun carNumberNotConfirmed() {

    }

    fun returnToListOrderClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToWarehouse
    }

    fun refuseOrderConfirmClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToWarehouse
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