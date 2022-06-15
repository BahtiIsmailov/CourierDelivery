package ru.wb.go.ui.couriercarnumber

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import ru.wb.go.app.DEFAULT_CAR_NUMBER
import ru.wb.go.app.DEFAULT_CAR_TYPE
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.couriercarnumber.domain.CourierCarNumberInteractor
import ru.wb.go.ui.couriercarnumber.keyboard.CarNumberKeyboardNumericView
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.formatter.CarNumberUtils

fun String.replaceCarNumberY(): String {
    return this.replace("Y", "У")
}

fun String.revertCarNumberY(): String {
    return this.replace("У", "Y")
}

class CourierCarNumberViewModel(
    private val parameters: CourierCarNumberParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierCarNumberInteractor,
    resourceProvider: CourierCarNumberResourceProvider,
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

    private var carNumber = DEFAULT_CAR_NUMBER
    private var carType = interactor.getCarType()
    private val types: List<CourierCarTypeItem> = run {
        val names = resourceProvider.getTypeNames()
        val icons = resourceProvider.getTypeIcons()
        val count = names.size
        val types = mutableListOf<CourierCarTypeItem>()
        for (i in 0 until count) {
            types.add(CourierCarTypeItem(icons.getResourceId(i, 0), names[i]))
        }
        types.toList()
    }

    init {
        updateCarType()
    }

    fun onCheckCarNumberClick() {
        putCarTypeAndNumber()
    }

    fun onCancelCarNumberClick() {
        fetchCarNumberComplete()
    }

    fun onCarTypeSelectClick() {
        _stateUI.value = CourierCarNumberUIState.InitTypeItems(types)
    }

    fun onCarTypeCloseClick() {
        _stateUI.value = CourierCarNumberUIState.CloseTypeItems
    }

    fun onNumberObservableClicked(event: CarNumberKeyboardNumericView.ButtonAction) {
        try {
            carNumber = accumulateNumber(interactor.getCarNumber(), event).replaceCarNumberY()
            switchBackspace(carNumber)
            switchComplete()
            _stateUI.value = carNumberFormat(CarNumberUtils(carNumber)).invoke()
        } catch (e: Exception) {
            onTechErrorLog("onNumberObservableClicked", e)
        }
    }

    fun onAddressItemClick(index: Int) {
        carType = index
        updateCarType()
    }

    private fun updateCarType() {
        if (carType == DEFAULT_CAR_TYPE) return
        switchComplete()
        _stateUI.value = CourierCarNumberUIState.SelectedCarType(types[carType])
    }

    private fun carNumberFormat(carNumberUtils: CarNumberUtils): () -> CourierCarNumberUIState =
        {
            CourierCarNumberUIState.NumberSpanFormat(
                carNumberUtils.numberWithMask(),
                carNumberUtils.numberSpanLength(),
                carNumberUtils.regionWithMask(),
                carNumberUtils.regionSpanLength(),
                carNumberUtils.numberKeyboardMode()
            )
        }

    private fun switchBackspace(it: String) {
        _stateKeyboardBackspaceUI.value =
            if (it.isEmpty()) CourierCarNumberBackspaceUIState.Inactive
            else CourierCarNumberBackspaceUIState.Active

    }

    private fun switchComplete() {
        _stateUI.value =
            if (carNumber.length < NUMBER_LENGTH_MAX - 1) CourierCarNumberUIState.NumberNotFilled
            else CourierCarNumberUIState.NumberFormatComplete
    }

    private fun accumulateNumber(
        accumulator: String,
        item: CarNumberKeyboardNumericView.ButtonAction
    ) = if (item == CarNumberKeyboardNumericView.ButtonAction.BUTTON_DELETE) {
        accumulator.dropLast(NUMBER_DROP_COUNT_LAST)
    } else if (item == CarNumberKeyboardNumericView.ButtonAction.BUTTON_DELETE_LONG) {
        accumulator.drop(accumulator.length)
    } else {
        if (accumulator.length > NUMBER_LENGTH_MAX - 1) accumulator.take(NUMBER_LENGTH_MAX)
        else accumulator.plus(item.symbol)
    }

    private fun putCarTypeAndNumber() {
        _progressState.value = CourierCarNumberProgressState.Progress
        try {
            interactor.putCarTypeAndNumber(
                carType,
                carNumber.replace("\\s".toRegex(), "").revertCarNumberY()
            )
            fetchCarNumberComplete()
        } catch (e: Exception) {
            fetchCarNumberError(e)
        }

    }

    private fun fetchCarNumberComplete() {
        onTechEventLog("fetchCarNumberComplete", "NavigateToTimer")
        _navigationState.value = CourierCarNumberNavigationState.NavigateToOrderDetails(
                result = parameters.result
            )
        _progressState.value = CourierCarNumberProgressState.ProgressComplete
    }

    private fun fetchCarNumberError(throwable: Throwable) {
        onTechErrorLog("fetchCarNumberError", throwable)
        _progressState.value = CourierCarNumberProgressState.ProgressComplete
        _navigationState.value =
            CourierCarNumberNavigationState.NavigateToOrderDetails(
                result = parameters.result
            )

    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val NUMBER_LENGTH_MAX = 9
        const val NUMBER_DROP_COUNT_LAST = 1
        const val SCREEN_TAG = "CourierCarNumber"
    }

    /*
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

    private var carNumber = DEFAULT_CAR_NUMBER
    private var carType = interactor.getCarType()
    private val types: List<CourierCarTypeItem> = run {
        val names = resourceProvider.getTypeNames()
        val icons = resourceProvider.getTypeIcons()
        val count = names.size
        val types = mutableListOf<CourierCarTypeItem>()
        for (i in 0 until count) {
            types.add(CourierCarTypeItem(icons.getResourceId(i, 0), names[i]))
        }
        types.toList()
    }

    init {
        updateCarType()
    }

    fun onCheckCarNumberClick() {
        putCarTypeAndNumber()
    }

    fun onCancelCarNumberClick() {
        fetchCarNumberComplete()
    }

    fun onCarTypeSelectClick() {
        _stateUI.value = CourierCarNumberUIState.InitTypeItems(types)
    }

    fun onCarTypeCloseClick() {
        _stateUI.value = CourierCarNumberUIState.CloseTypeItems
    }

    fun onNumberObservableClicked(event: Observable<CarNumberKeyboardNumericView.ButtonAction>) {
        addSubscription(
            event.scan(interactor.getCarNumber()) { accumulator, item ->
                accumulateNumber(accumulator, item)
            }
                .map { it.replaceCarNumberY() }
                .doOnNext { carNumber = it }
                .doOnNext {
                    switchBackspace(it)
                    switchComplete()
                }
                .map { carNumberFormat(CarNumberUtils(it)).invoke() }
                .subscribe(
                    { _stateUI.value = it },
                    { onTechErrorLog("onNumberObservableClicked", it) })
        )
    }

    fun onAddressItemClick(index: Int) {
        carType = index
        updateCarType()
    }

    private fun updateCarType() {
        if (carType == DEFAULT_CAR_TYPE) return
        switchComplete()
        _stateUI.postValue(CourierCarNumberUIState.SelectedCarType(types[carType]))
    }

    private fun carNumberFormat(carNumberUtils: CarNumberUtils): () -> CourierCarNumberUIState =
        {
            CourierCarNumberUIState.NumberSpanFormat(
                carNumberUtils.numberWithMask(),
                carNumberUtils.numberSpanLength(),
                carNumberUtils.regionWithMask(),
                carNumberUtils.regionSpanLength(),
                carNumberUtils.numberKeyboardMode()
            )
        }

    private fun switchBackspace(it: String) {
        _stateKeyboardBackspaceUI.value =
            if (it.isEmpty()) CourierCarNumberBackspaceUIState.Inactive
            else CourierCarNumberBackspaceUIState.Active
    }

    private fun switchComplete() {
        _stateUI.value =
                //выключено до реализации на сервере
//            if (carNumber.length < NUMBER_LENGTH_MAX - 1 || carType == -1) CourierCarNumberUIState.NumberNotFilled
            if (carNumber.length < NUMBER_LENGTH_MAX - 1) CourierCarNumberUIState.NumberNotFilled
            else CourierCarNumberUIState.NumberFormatComplete
    }

    private fun accumulateNumber(
        accumulator: String,
        item: CarNumberKeyboardNumericView.ButtonAction
    ) = if (item == CarNumberKeyboardNumericView.ButtonAction.BUTTON_DELETE) {
        accumulator.dropLast(NUMBER_DROP_COUNT_LAST)
    } else if (item == CarNumberKeyboardNumericView.ButtonAction.BUTTON_DELETE_LONG) {
        accumulator.drop(accumulator.length)
    } else {
        if (accumulator.length > NUMBER_LENGTH_MAX - 1) accumulator.take(NUMBER_LENGTH_MAX)
        else accumulator.plus(item.symbol)
    }

    private fun putCarTypeAndNumber() {
        _progressState.value = CourierCarNumberProgressState.Progress
        addSubscription(interactor.putCarTypeAndNumber(
            carType,
            carNumber.replace("\\s".toRegex(), "").revertCarNumberY()
        )
            .subscribe(
                { fetchCarNumberComplete() },
                { fetchCarNumberError(it) }
            )
        )
    }

    private fun fetchCarNumberComplete() {
        onTechEventLog("fetchCarNumberComplete", "NavigateToTimer")
        _navigationState.value = CourierCarNumberNavigationState.NavigateToOrderDetails(
            result = parameters.result
        )
        _progressState.value = CourierCarNumberProgressState.ProgressComplete
    }

    private fun fetchCarNumberError(throwable: Throwable) {
        onTechErrorLog("fetchCarNumberError", throwable)
        _progressState.value = CourierCarNumberProgressState.ProgressComplete
        _navigationState.value = CourierCarNumberNavigationState.NavigateToOrderDetails(
            result = parameters.result
        )
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val NUMBER_LENGTH_MAX = 9
        const val NUMBER_DROP_COUNT_LAST = 1
        const val SCREEN_TAG = "CourierCarNumber"
    }


     */
}