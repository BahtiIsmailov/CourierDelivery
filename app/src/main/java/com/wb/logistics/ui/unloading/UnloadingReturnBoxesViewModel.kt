package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.dcloading.DcLoadingResourceProvider
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.utils.time.TimeFormatType
import com.wb.logistics.utils.time.TimeFormatter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class UnloadingReturnBoxesViewModel(
    private val parameters: UnloadingReturnParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: UnloadingInteractor,
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: DcLoadingResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _boxes = MutableLiveData<UnloadingReturnBoxesUIState>()
    val boxes: LiveData<UnloadingReturnBoxesUIState>
        get() = _boxes

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    private val _navigateToMessage = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessage: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessage

    private val _enableRemove = MutableLiveData<Boolean>()
    val enableRemove: LiveData<Boolean>
        get() = _enableRemove

    private var copyReceptionBoxes = mutableListOf<UnloadingReturnBoxesItem>()

    init {
        addSubscription(interactor.observeReturnBoxes(parameters.dstOfficeId)
            .flatMap { convertBoxes(it) }
            .doOnNext { copyConvertBoxes(it) }
            .subscribe({ changeBoxesComplete(it) },
                { changeBoxesError(it) }))
    }

    private fun convertBoxes(boxes: List<FlightBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()
            .toObservable()

    private val receptionBoxItem = { (index, item): IndexedValue<FlightBoxEntity> ->
        val date = timeFormatter.dateTimeWithoutTimezoneFromString(item.updatedAt)
        val timeFormat = resourceProvider.getBoxDateAndTime(
            timeFormatter.format(date, TimeFormatType.ONLY_DATE),
            timeFormatter.format(date, TimeFormatType.ONLY_TIME))
        UnloadingReturnBoxesItem(
            item.barcode,
            resourceProvider.getUnnamedBarcodeFormat(singleIncrement(index), item.barcode),
            timeFormat,
            false)
    }

    private val singleIncrement = { index: Int -> index + 1 }

    private fun copyConvertBoxes(boxes: List<UnloadingReturnBoxesItem>) {
        copyReceptionBoxes = boxes.toMutableList()
    }

    private fun changeBoxesComplete(boxes: MutableList<UnloadingReturnBoxesItem>) {
        if (boxes.isEmpty()) {
            _boxes.value = UnloadingReturnBoxesUIState.Empty
        } else {
            _boxes.value = UnloadingReturnBoxesUIState.ReceptionBoxesItem(boxes)
        }
    }

    private fun changeBoxesError(error: Throwable) {
        LogUtils { logDebugApp(error.toString()) }
    }

    fun onRemoveClick() {
        _boxes.value = UnloadingReturnBoxesUIState.Progress
        val checkedReturnBoxes =
            copyReceptionBoxes.filter { it.isChecked }.map { it.barcode }.toMutableList()
        addSubscription(interactor.removeReturnBoxes(parameters.dstOfficeId, checkedReturnBoxes)
            .subscribe(
                { removeReturnBoxesComplete() },
                { removeReturnBoxesError(it) }
            )
        )
    }

    private fun removeReturnBoxesComplete() {
        _boxes.value = UnloadingReturnBoxesUIState.ProgressComplete
        _navigateToBack.value = NavigateToBack
    }

    private fun removeReturnBoxesError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getErrorRemovedBoxesDialogMessage()
        }
        _navigateToMessage.value = NavigateToMessageInfo(
            resourceProvider.getBoxDialogTitle(),
            message,
            resourceProvider.getBoxPositiveButton())
        _boxes.value = UnloadingReturnBoxesUIState.ProgressComplete
        changeDisableAllCheckedBox()
    }

    fun onItemClick(index: Int, checked: Boolean) {
        changeCheckedBox(index, checked)
        changeEnableRemove()
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
        _boxes.value = UnloadingReturnBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    private fun changeCheckedBox(index: Int, checked: Boolean) {
        val copyReception = copyReceptionBoxes[index].copy(isChecked = checked)
        copyReceptionBoxes[index] = copyReception
        _boxes.value = UnloadingReturnBoxesUIState.ReceptionBoxItem(index, copyReception)
    }

    object NavigateToBack

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

    data class Label(val label: String)

}