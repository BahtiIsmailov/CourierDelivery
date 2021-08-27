package ru.wb.perevozka.ui.courierordertimer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.CheckSmsViewModel
import ru.wb.perevozka.ui.courierorderdetails.domain.CourierOrderDetailsInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle
import ru.wb.perevozka.utils.LogUtils
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CourierOrderTimerViewModel(
    private val parameters: CourierOrderTimerParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderDetailsInteractor,
    private val resourceProvider: CourierOrderTimerResourceProvider,
) : NetworkViewModel(compositeDisposable) {

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

    companion object {
        const val DELAY_TIMER_SEC = 20L * 60
    }

    init {
        initOrderInfo()
        initTimer()
    }

    private fun initTimer() {
        addSubscription(
            Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .take(DELAY_TIMER_SEC)
                .scan(0, { accumulator, item -> accumulator + 1 })
                .subscribe({
                    val invertTime = (DELAY_TIMER_SEC - it).toInt()
                    val min = String.format(Locale.getDefault(), "%02d", getMin(invertTime))
                    val sec = String.format(Locale.getDefault(), "%02d", getSec(invertTime))
                    val timeAnalog = (100.0F / DELAY_TIMER_SEC) * invertTime
                    val timeDigit = "$min:$sec"
                    _orderTimer.value = CourierOrderTimerState.Timer(timeAnalog, timeDigit)
                },
                    {
                        LogUtils { logDebugApp("initTimer err " + it.toString()) }
                    })
        )
    }

    private fun getMin(duration: Int): Int {
        return duration / CheckSmsViewModel.TIME_DIVIDER
    }

    private fun getSec(duration: Int): Int {
        return duration % CheckSmsViewModel.TIME_DIVIDER
    }

    private fun initOrderInfo() {
        with(parameters.order) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderTimerInfoUIState.InitOrderInfo(
                resourceProvider.getOrder(id),
                srcOffice.name,
                resourceProvider.getCoast(coast),
                resourceProvider.getBoxCountAndVolume(minBoxesCount, minVolume),
                resourceProvider.getPvz(dstOffices.size)
            )
        }
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderTimerProgressState.ProgressComplete
    }

    fun refuseOrderClick() {

    }

    fun confirmTakeOrderClick() {
        _progressState.value = CourierOrderTimerProgressState.Progress
        addSubscription(
            interactor.anchorTask(parameters.order.id.toString())
                .subscribe({
                    _progressState.value = CourierOrderTimerProgressState.ProgressComplete
                }, {
                    _progressState.value = CourierOrderTimerProgressState.ProgressComplete

                    // TODO: 25.08.2021 выключено до полной реализации экранов номера автомобиля
//                    _navigationState.value = CourierOrderDetailsNavigationState.NavigateToDialogInfo(
//                        DialogStyle.WARNING.ordinal,
//                        "Заказ забрали",
//                        "Этот заказ уже взят в работу",
//                        "Вернуться к списку заказов"
//                    )
                    _navigationState.value = CourierOrderTimerNavigationState.NavigateToCarNumber
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