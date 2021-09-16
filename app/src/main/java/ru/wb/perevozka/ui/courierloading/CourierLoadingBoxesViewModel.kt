package ru.wb.perevozka.ui.courierloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.courierloading.domain.CourierLoadingInteractor
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.time.TimeFormatType
import ru.wb.perevozka.utils.time.TimeFormatter

class CourierLoadingBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierLoadingInteractor,
    private val resourceProvider: CourierLoadingResourceProvider,
    private val timeFormatter: TimeFormatter,
) : NetworkViewModel(compositeDisposable) {

    private val _boxes = MutableLiveData<CourierLoadingBoxesUIState>()
    val boxes: LiveData<CourierLoadingBoxesUIState>
        get() = _boxes

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessage: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _enableRemove = MutableLiveData<Boolean>()
    val enableRemove: LiveData<Boolean>
        get() = _enableRemove

    private var copyReceptionBoxes = mutableListOf<CourierLoadingBoxesItem>()

    init {
        observeNetworkState()
        observeScannedBoxes()
    }

    private fun observeScannedBoxes() {
        addSubscription(interactor.scannedBoxes()
            .flatMap { convertBoxes(it) }
            .doOnSuccess { copyConvertBoxes(it) }
            .subscribe({ changeBoxesComplete(it) },
                { changeBoxesError(it) })
        )
    }

    private fun convertBoxes(boxes: List<CourierBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()

    private val receptionBoxItem = { (index, item): IndexedValue<CourierBoxEntity> ->
        val date = timeFormatter.dateTimeWithoutTimezoneFromString(item.loadingAt)
        val dateFormat = timeFormatter.format(date, TimeFormatType.ONLY_DATE)
        val timeFormat = timeFormatter.format(date, TimeFormatType.ONLY_TIME)
        CourierLoadingBoxesItem(
            resourceProvider.getIndex(singleIncrement(index)),
            item.id,
            resourceProvider.getBoxDateAndTimeAndAddress(dateFormat, timeFormat, item.address),
            false
        )
    }

    private val singleIncrement = { index: Int -> index + 1 }

    private fun copyConvertBoxes(boxes: List<CourierLoadingBoxesItem>) {
        copyReceptionBoxes = boxes.toMutableList()
    }

    private fun changeBoxesComplete(boxes: MutableList<CourierLoadingBoxesItem>) {
        _boxes.value = if (boxes.isEmpty()) CourierLoadingBoxesUIState.Empty
        else CourierLoadingBoxesUIState.ReceptionBoxesItem(boxes)
    }

    private fun changeBoxesError(error: Throwable) {
        LogUtils { logDebugApp(error.toString()) }
    }

    fun onRemoveClick() {
        _boxes.value = CourierLoadingBoxesUIState.Progress
        val loadingBoxes =
            copyReceptionBoxes.filter { it.isChecked }.map { it.qrCode }.toMutableList()
        addSubscription(interactor.removeScannedBoxes(loadingBoxes)
            .subscribe(
                { removeScannedBoxesComplete() },
                { removeScannedBoxesError(it) }
            )
        )
    }

    private fun removeScannedBoxesComplete() {
        _boxes.value = CourierLoadingBoxesUIState.ProgressComplete
        _navigateToBack.value = NavigateToBack
    }

    private fun removeScannedBoxesError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getErrorRemovedBoxesDialogMessage()
        }
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getBoxDialogTitle(),
            message,
            resourceProvider.getBoxPositiveButton()
        )
        _boxes.value = CourierLoadingBoxesUIState.ProgressComplete
        changeDisableAllCheckedBox()
    }

    fun onItemClick(index: Int, checked: Boolean) {
        changeCheckedBox(index, checked)
        changeEnableRemove()
    }

    private fun changeCheckedBox(index: Int, checked: Boolean) {
        val copyReception = copyReceptionBoxes[index].copy(isChecked = checked)
        copyReceptionBoxes[index] = copyReception
        _boxes.value = CourierLoadingBoxesUIState.ReceptionBoxItem(index, copyReception)
    }

    private fun changeEnableRemove() {
        var activeRemove = false
        copyReceptionBoxes.forEach {
            if (it.isChecked) {
                activeRemove = it.isChecked
                return@forEach
            }
        }
        _enableRemove.value = activeRemove
    }

    private fun changeDisableAllCheckedBox() {
        copyReceptionBoxes.forEachIndexed { index, _ ->
            val copyReception = copyReceptionBoxes[index].copy(isChecked = false)
            copyReceptionBoxes[index] = copyReception
        }
        _boxes.value = CourierLoadingBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    object NavigateToBack

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}