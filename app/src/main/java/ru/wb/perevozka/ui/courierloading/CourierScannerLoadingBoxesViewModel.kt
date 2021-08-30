package ru.wb.perevozka.ui.courierloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.dcloading.domain.DcLoadingInteractor
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.time.TimeFormatType
import ru.wb.perevozka.utils.time.TimeFormatter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class CourierScannerLoadingBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: DcLoadingInteractor,
    private val resourceProvider: CourierScannerLoadingResourceProvider,
    private val timeFormatter: TimeFormatter,
) : NetworkViewModel(compositeDisposable) {

    private val _boxes = MutableLiveData<CourierScannerLoadingBoxesUIState>()
    val boxes: LiveData<CourierScannerLoadingBoxesUIState>
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

    private var copyReceptionBoxes = mutableListOf<CourierScannerLoadingBoxesItem>()

    init {
        observeNetworkState()
        observeScannedBoxes()
    }

    private fun observeScannedBoxes() {
        addSubscription(interactor.observeScannedBoxes()
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
        val dateFormat = resourceProvider.getBoxDateAndTime(
            timeFormatter.format(date, TimeFormatType.ONLY_DATE),
            timeFormatter.format(date, TimeFormatType.ONLY_TIME))
        CourierScannerLoadingBoxesItem(
            item.barcode,
            resourceProvider.getIndexUnnamedBarcode(singleIncrement(index), item.barcode),
            resourceProvider.getBoxTimeAndAddress(dateFormat, item.dstOffice.fullAddress),
            false)
    }

    private val singleIncrement = { index: Int -> index + 1 }

    private fun copyConvertBoxes(boxes: List<CourierScannerLoadingBoxesItem>) {
        copyReceptionBoxes = boxes.toMutableList()
    }

    private fun changeBoxesComplete(boxes: MutableList<CourierScannerLoadingBoxesItem>) {
        _boxes.value = if (boxes.isEmpty()) CourierScannerLoadingBoxesUIState.Empty
        else CourierScannerLoadingBoxesUIState.ReceptionBoxesItem(boxes)
    }

    private fun changeBoxesError(error: Throwable) {
        LogUtils { logDebugApp(error.toString()) }
    }

    fun onRemoveClick() {
        _boxes.value = CourierScannerLoadingBoxesUIState.Progress
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
        _boxes.value = CourierScannerLoadingBoxesUIState.ProgressComplete
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
            resourceProvider.getBoxPositiveButton())
        _boxes.value = CourierScannerLoadingBoxesUIState.ProgressComplete
        changeDisableAllCheckedBox()
    }

    fun onItemClick(index: Int, checked: Boolean) {
        changeCheckedBox(index, checked)
        changeEnableRemove()
    }

    private fun changeCheckedBox(index: Int, checked: Boolean) {
        val copyReception = copyReceptionBoxes[index].copy(isChecked = checked)
        copyReceptionBoxes[index] = copyReception
        _boxes.value = CourierScannerLoadingBoxesUIState.ReceptionBoxItem(index, copyReception)
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
        _boxes.value = CourierScannerLoadingBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected()
            .subscribe({ _toolbarNetworkState.value = it }, {}))
    }

    object NavigateToBack

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}