package ru.wb.perevozka.ui.courierordertimer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierTimerEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateHandler
import ru.wb.perevozka.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.ui.dialogs.NavigateToMessageInfo
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.time.DateTimeFormatter
import java.text.DecimalFormat

class CourierOrderTimerViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderTimerInteractor,
    private val resourceProvider: CourierOrderTimerResourceProvider,
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
            interactor.timerEntity()
                .subscribe(
                    {
                        initOrderInfo(it)
                        initTimer(it.reservedDuration, it.reservedAt)
                    }, {
                        LogUtils {logDebugApp("initOrder() error " + it)}
                    }
                )
        )
    }

    private fun initTimer(reservedDuration: String, reservedAt: String) {
        updateTimer(0, 0)
        interactor.startTimer(reservedDuration, reservedAt)
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

    private fun initOrderInfo(courierTimerEntity: CourierTimerEntity) {
        with(courierTimerEntity) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(price)
            _orderInfo.value = CourierOrderTimerInfoUIState.InitOrderInfo(
                resourceProvider.getOrder(orderId),
                name,
                resourceProvider.getCoast(coast),
                resourceProvider.getBoxCountAndVolume(boxesCount, volume),
                resourceProvider.getPvz(countPvz),
                if (gate.isEmpty()) "-" else gate
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

//    data class NavigateToMessageInfo(
//        val type: Int,
//        val title: String,
//        val message: String,
//        val button: String
//    )

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