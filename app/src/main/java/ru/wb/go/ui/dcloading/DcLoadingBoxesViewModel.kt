package ru.wb.go.ui.dcloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.dcloading.domain.DcLoadingInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToInformation
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.time.TimeFormatType
import ru.wb.go.utils.time.TimeFormatter

class DcLoadingBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: DcLoadingInteractor,
    private val resourceProvider: DcLoadingResourceProvider,
    private val timeFormatter: TimeFormatter,
) : NetworkViewModel(compositeDisposable) {

    private val _boxes = MutableLiveData<DcLoadingBoxesUIState>()
    val boxes: LiveData<DcLoadingBoxesUIState>
        get() = _boxes

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigateToMessageInfo = MutableLiveData<NavigateToInformation>()
    val navigateToMessage: LiveData<NavigateToInformation>
        get() = _navigateToMessageInfo

    private val _enableRemove = MutableLiveData<Boolean>()

    val enableRemove: LiveData<Boolean>
        get() = _enableRemove

    private var copyReceptionBoxes = mutableListOf<DcLoadingBoxesItem>()

    init {
        observeNetworkState()
        observeScannedBoxes()
    }

    private fun observeScannedBoxes() {
        addSubscription(interactor.observeScannedBoxes()
            .flatMap { convertBoxes(it) }
            .doOnNext { copyConvertBoxes(it) }
            .subscribe({ changeBoxesComplete(it) },
                { changeBoxesError(it) })
        )
    }

    private fun convertBoxes(boxes: List<FlightBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()
            .toObservable()

    private val receptionBoxItem = { (index, item): IndexedValue<FlightBoxEntity> ->
        val date = timeFormatter.dateTimeWithoutTimezoneFromString(item.updatedAt)
        val dateFormat = resourceProvider.getBoxDateAndTime(
            timeFormatter.format(date, TimeFormatType.ONLY_DATE),
            timeFormatter.format(date, TimeFormatType.ONLY_TIME)
        )
        DcLoadingBoxesItem(
            item.barcode,
            resourceProvider.getIndexUnnamedBarcode(singleIncrement(index), item.barcode),
            resourceProvider.getBoxTimeAndAddress(dateFormat, item.dstOffice.fullAddress),
            false
        )
    }

    private val singleIncrement = { index: Int -> index + 1 }

    private fun copyConvertBoxes(boxes: List<DcLoadingBoxesItem>) {
        copyReceptionBoxes = boxes.toMutableList()
    }

    private fun changeBoxesComplete(boxes: MutableList<DcLoadingBoxesItem>) {
        _boxes.value = if (boxes.isEmpty()) DcLoadingBoxesUIState.Empty
        else DcLoadingBoxesUIState.ReceptionBoxesItem(boxes)
    }

    private fun changeBoxesError(error: Throwable) {
        LogUtils { logDebugApp(error.toString()) }
    }

    fun onRemoveClick() {
        _boxes.value = DcLoadingBoxesUIState.Progress
        val dcLoadingBoxes =
            copyReceptionBoxes.filter { it.isChecked }.map { it.barcode }.toMutableList()
        addSubscription(interactor.removeScannedBoxes(dcLoadingBoxes)
            .subscribe(
                { removeScannedBoxesComplete() },
                { removeScannedBoxesError(it) }
            )
        )
    }

    private fun removeScannedBoxesComplete() {
        _boxes.value = DcLoadingBoxesUIState.ProgressComplete
        _navigateToBack.value = NavigateToBack
    }

    private fun removeScannedBoxesError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getErrorRemovedBoxesDialogMessage()
        }
        _navigateToMessageInfo.value = NavigateToInformation(
            DialogInfoStyle.ERROR.ordinal,
            resourceProvider.getBoxDialogTitle(),
            message,
            resourceProvider.getBoxPositiveButton()
        )
        _boxes.value = DcLoadingBoxesUIState.ProgressComplete
        changeDisableAllCheckedBox()
    }

    fun onItemClick(index: Int, checked: Boolean) {
        changeCheckedBox(index, checked)
        changeEnableRemove()
    }

    private fun changeCheckedBox(index: Int, checked: Boolean) {
        val copyReception = copyReceptionBoxes[index].copy(isChecked = checked)
        copyReceptionBoxes[index] = copyReception
        _boxes.value = DcLoadingBoxesUIState.ReceptionBoxItem(index, copyReception)
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
        _boxes.value = DcLoadingBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected()
            .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    object NavigateToBack

}