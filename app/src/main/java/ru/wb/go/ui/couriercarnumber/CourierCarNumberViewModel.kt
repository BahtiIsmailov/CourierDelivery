package ru.wb.go.ui.couriercarnumber

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriercarnumber.domain.CourierCarNumberInteractor
import ru.wb.go.ui.couriercarnumber.keyboard.CarNumberKeyboardNumericView
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.formatter.CarNumberUtils

class CourierCarNumberViewModel(
    private val parameters: CourierCarNumberParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierCarNumberResourceProvider,
    private val interactor: CourierCarNumberInteractor,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _navigationState =
        SingleLiveEvent<CourierCarNumberNavigationState>()
    val navigationState: LiveData<CourierCarNumberNavigationState>
        get() = _navigationState

    private val _stateUI = SingleLiveEvent<CourierCarNumberUIState>()
    val stateUI: LiveData<CourierCarNumberUIState>
        get() = _stateUI

    private val _stateKeyboardBackspaceUI = SingleLiveEvent<CourierCarNumberBackspaceUIState>()
    val stateBackspaceUI: LiveData<CourierCarNumberBackspaceUIState>
        get() = _stateKeyboardBackspaceUI

    private val _progressState = MutableLiveData<CourierCarNumberProgressState>()
    val progressState: LiveData<CourierCarNumberProgressState>
        get() = _progressState

    fun onCheckCarNumberClick() {
        putCarNumber(carNumber)
    }

    private var carNumber = ""

    fun onNumberObservableClicked(event: Observable<CarNumberKeyboardNumericView.ButtonAction>) {
        addSubscription(
            event.scan(String()) { accumulator, item -> accumulateNumber(accumulator, item) }
                .doOnNext {
                    switchBackspace(it)
                    switchComplete(it)
                }
                .doOnNext { carNumber = it }
                .map { keyToNumberSpanFormat(it) }
                .subscribe(
                    { _stateUI.value = it },
                    { onTechErrorLog("onNumberObservableClicked", it) })
        )
    }

    private fun keyToNumberSpanFormat(it: String) =
        CourierCarNumberUIState.NumberSpanFormat(
            CarNumberUtils.numberFormatter(it),
            CarNumberUtils.numberFormatterSpanLength(it),
            CarNumberUtils.numberKeyboardMode(it)
        )

    private fun switchBackspace(it: String) {
        _stateKeyboardBackspaceUI.value =
            if (it.isEmpty()) CourierCarNumberBackspaceUIState.Inactive
            else CourierCarNumberBackspaceUIState.Active
    }

    private fun switchComplete(it: String) {
        _stateUI.value =
            if (it.length < NUMBER_LENGTH_MAX - 1) CourierCarNumberUIState.NumberNotFilled
            else CourierCarNumberUIState.NumberFormatComplete
    }

    private fun accumulateNumber(
        accumulator: String,
        item: CarNumberKeyboardNumericView.ButtonAction
    ) =
        if (item == CarNumberKeyboardNumericView.ButtonAction.BUTTON_DELETE) {
            accumulator.dropLast(NUMBER_DROP_COUNT_LAST)
        } else if (item == CarNumberKeyboardNumericView.ButtonAction.BUTTON_DELETE_LONG) {
            accumulator.drop(accumulator.length)
        } else {
            if (accumulator.length > NUMBER_LENGTH_MAX - 1) accumulator.take(NUMBER_LENGTH_MAX)
            else accumulator.plus(item.symbol)
        }

    private fun putCarNumber(carNumber: String) {
        _progressState.value = CourierCarNumberProgressState.Progress
        val disposable = interactor.putCarNumber(carNumber.replace("\\s".toRegex(), ""))
            .subscribe(
                { fetchCarNumberComplete() },
                { fetchCarNumberError(it) }
            )
        addSubscription(disposable)
    }

    private fun fetchCarNumberComplete() {
        onTechEventLog("fetchCarNumberComplete", "NavigateToTimer")
        _navigationState.value = CourierCarNumberNavigationState.NavigateToOrderDetails(
            title = parameters.title, orderNumber = parameters.orderNumber, order = parameters.order
        )
        _progressState.value = CourierCarNumberProgressState.ProgressComplete
    }

    private fun fetchCarNumberError(throwable: Throwable) {
        onTechErrorLog("fetchCarNumberError", throwable)
        _progressState.value = CourierCarNumberProgressState.ProgressComplete
        _navigationState.value = CourierCarNumberNavigationState.NavigateToOrderDetails(
            title = parameters.title, orderNumber = parameters.orderNumber, order = parameters.order
        )
    }

    fun onCancelLoadClick() {
        onTechEventLog("onCancelLoadClick")
        clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val NUMBER_LENGTH_MAX = 9
        const val NUMBER_DROP_COUNT_LAST = 1
        const val SCREEN_TAG = "CourierCarNumber"
    }

}