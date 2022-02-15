package ru.wb.go.ui.courierordertimer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateHandler
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.time.DateTimeFormatter
import java.text.DecimalFormat

class CourierOrderTimerViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrderTimerInteractor,
    private val resourceProvider: CourierOrderTimerResourceProvider,
    private val errorDialogManager: ErrorDialogManager
) : TimerStateHandler, NetworkViewModel(compositeDisposable, metric) {

    private val _orderTimer = MutableLiveData<CourierOrderTimerState>()
    val orderTimer: LiveData<CourierOrderTimerState>
        get() = _orderTimer

    private val _orderInfo = MutableLiveData<CourierOrderTimerInfoUIState>()
    val orderInfo: LiveData<CourierOrderTimerInfoUIState>
        get() = _orderInfo

    private val _navigateToDialogTimeIsOut = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogTimeIsOut: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogTimeIsOut


    private val _navigateToDialogRefuseOrder = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogRefuseOrder: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogRefuseOrder

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _navigationState = SingleLiveEvent<CourierOrderTimerNavigationState>()
    val navigationState: LiveData<CourierOrderTimerNavigationState>
        get() = _navigationState

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _timeOut = SingleLiveEvent<Boolean>()
        .apply { value = false }
    val timeOut: LiveData<Boolean> get() = _timeOut

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
                    },
                    {
                        onTechErrorLog("initOrder", it)
                    }
                )
        )
    }

    private fun initTimer(reservedDuration: String, reservedAt: String) {
        updateTimer(0, 0)
        interactor.startTimer(reservedDuration, reservedAt)
        addSubscription(
            interactor.timer
                .subscribe({ onHandleSignUpState(it) }, { onHandleSignUpError(it) })
        )
    }

    private fun onHandleSignUpState(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun onHandleSignUpError(throwable: Throwable) {
        onTechErrorLog("onHandleSignUpError", throwable)
    }

    private fun timeIsOut() {
        _timeOut.postValue(true)
        _navigateToDialogTimeIsOut.value = NavigateToDialogInfo(
            DialogInfoStyle.WARNING.ordinal,
            resourceProvider.getDialogTimerTitle(),
            resourceProvider.getDialogTimerMessage(),
            resourceProvider.getDialogTimerButton()
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

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    fun onRefuseOrderClick() {

        _navigateToDialogRefuseOrder.value = NavigateToDialogConfirmInfo(
            DialogInfoStyle.WARNING.ordinal,
            resourceProvider.getDialogTimerSkipTitle(),
            resourceProvider.getDialogTimerSkipMessage(),
            resourceProvider.getDialogTimerPositiveButton(),
            resourceProvider.getDialogTimerNegativeButton()
        )
    }

    fun iArrivedClick() {
        _navigationState.value = CourierOrderTimerNavigationState.NavigateToScanner
    }

    fun timeOutReturnToList() {
        onTechEventLog("onReturnToListOrderClick")
        deleteTask()
    }

    fun onRefuseOrderConfirmClick() {
        onTechEventLog("onRefuseOrderConfirmClick")
        deleteTask()
    }

    private fun deleteTask() {

        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.deleteTask()
                .subscribe(
                    {
                        setLoader(WaitLoader.Complete)
                        onTechEventLog("toWarehouse")
                        _navigationState.value =
                            CourierOrderTimerNavigationState.NavigateToWarehouse
                        _timeOut.postValue(false)
                    },
                    {
                        setLoader(WaitLoader.Complete)
                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)

                    }
                )
        )
    }

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
        onTechEventLog("onTimeIsOverState")
        timeIsOut()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrderTimer"
    }

}