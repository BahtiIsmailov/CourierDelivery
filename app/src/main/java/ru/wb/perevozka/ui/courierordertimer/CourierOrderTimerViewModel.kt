package ru.wb.perevozka.ui.courierordertimer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.DEFAULT_ARRIVAL_TIME_COURIER_MIN
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateHandler
import ru.wb.perevozka.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.utils.time.DateTimeFormatter
import ru.wb.perevozka.utils.time.TimeFormatter
import java.text.DecimalFormat

class CourierOrderTimerViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderTimerInteractor,
    private val resourceProvider: CourierOrderTimerResourceProvider,
    private val timeFormatter: TimeFormatter
) : TimerStateHandler, NetworkViewModel(compositeDisposable) {

    private val _orderTimer = MutableLiveData<CourierOrderTimerState>()
    val orderTimer: LiveData<CourierOrderTimerState>
        get() = _orderTimer

    private val _orderInfo = MutableLiveData<CourierOrderTimerInfoUIState>()
    val orderInfo: LiveData<CourierOrderTimerInfoUIState>
        get() = _orderInfo

    private val _navigationState = SingleLiveEvent<CourierOrderTimerNavigationState>()
    val navigationState: LiveData<CourierOrderTimerNavigationState>
        get() = _navigationState

    private val _progressState = MutableLiveData<CourierOrderTimerProgressState>()
    val progressState: LiveData<CourierOrderTimerProgressState>
        get() = _progressState


    init {
        initOrder()
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeOrderData()
                .subscribe(
                    {
                        initOrderInfo(it.courierOrderLocalEntity, it.dstOffices.size)
                        initTimer(
                            it.courierOrderLocalEntity.reservedDuration,
                            it.courierOrderLocalEntity.reservedAt
                        )
                    }, {}
                )
        )
    }

    private fun initTimer(reservedDuration: String, reservedAt: String) {
        var arrivalSec: Long
        val durationSec = (reservedDuration.toIntOrNull() ?: DEFAULT_ARRIVAL_TIME_COURIER_MIN) * 60L
        arrivalSec = if (reservedAt.isEmpty()) {
            durationSec
        } else {
            val reservedAtDataTime =
                timeFormatter.dateTimeWithoutTimezoneFromString(reservedAt).millis
            val currentDateTime = timeFormatter.currentDateTime().millis
            val offsetSec = (currentDateTime - reservedAtDataTime) / 1000
            durationSec - offsetSec
        }
        if (arrivalSec < 0) arrivalSec = 0L
        updateTimer(durationSec.toInt(), arrivalSec.toInt())
        interactor.startTimer(durationSec.toInt(), arrivalSec.toInt())
        addSubscription(
            interactor.timer
                .subscribe({ onHandleSignUpState(it) }, { onHandleSignUpError() })
        )
    }

    private fun onHandleSignUpState(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun onHandleSignUpError() {}

    private fun timeIsOut() {
        _navigationState.value = CourierOrderTimerNavigationState.NavigateToDialogInfo(
            DialogStyle.WARNING.ordinal,
            "Время вышло",
            "К сожалению, вы не успели приехать вовремя. Заказ был отменён",
            "Вернуться к списку заказов"
        )
    }

    private fun initOrderInfo(courierOrderLocalEntity: CourierOrderLocalEntity, pvz: Int) {
        with(courierOrderLocalEntity) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderTimerInfoUIState.InitOrderInfo(
                resourceProvider.getOrder(id),
                srcOffice.name,
                resourceProvider.getCoast(coast),
                resourceProvider.getBoxCountAndVolume(minBoxesCount, minVolume),
                resourceProvider.getPvz(pvz)
            )
        }
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderTimerProgressState.ProgressComplete
    }

    fun refuseOrderClick() {
        _navigationState.value = CourierOrderTimerNavigationState.NavigateToRefuseOrderDialog(
            "Отказаться от заказа",
            "Вы уверены, что хотите отказаться от заказа?"
        )
    }

    fun iArrivedClick() {
        _navigationState.value = CourierOrderTimerNavigationState.NavigateToScanner
    }

    fun returnToListOrderClick() {
        deleteTask()
    }

    fun refuseOrderConfirmClick() {
        deleteTask()
    }

    private fun deleteTask() {
        addSubscription(interactor.deleteTask().subscribe(
            { toWarehouse() }, {})
        )
    }

    private fun toWarehouse() {
        _navigationState.value = CourierOrderTimerNavigationState.NavigateToWarehouse
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

    override fun onTimerState(duration: Int, downTickSec: Int) {
        updateTimer(duration, downTickSec)
    }

    private fun updateTimer(duration: Int, downTickSec: Int) {
        _orderTimer.value = CourierOrderTimerState.Timer(
            DateTimeFormatter.getAnalogTime(duration, downTickSec),
            DateTimeFormatter.getDigitTime(downTickSec)
        )
    }

    override fun onTimeIsOverState() {
        timeIsOut()
    }

}