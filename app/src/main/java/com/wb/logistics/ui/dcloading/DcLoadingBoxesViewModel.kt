package com.wb.logistics.ui.dcloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.dcloading.domain.DcLoadingInteractor
import com.wb.logistics.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class DcLoadingBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val receptionInteractor: DcLoadingInteractor,
    private val resourceProvider: DcLoadingResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _boxes = MutableLiveData<DcLoadingBoxesUIState>()
    val boxes: LiveData<DcLoadingBoxesUIState>
        get() = _boxes

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessage: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _enableRemove = MutableLiveData<Boolean>()

    val enableRemove: LiveData<Boolean>
        get() = _enableRemove

    private var copyReceptionBoxes = mutableListOf<DcLoadingBoxesItem>()

    init {
        addSubscription(receptionInteractor.observeScannedBoxes()
            .flatMap { convertBoxes(it) }
            .doOnNext { copyConvertBoxes(it) }
            .subscribe({ changeBoxesComplete(it) },
                { changeBoxesError(it) }))
    }

    private fun convertBoxes(boxes: List<AttachedBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()
            .toObservable()

    private val receptionBoxItem = { (index, item): IndexedValue<AttachedBoxEntity> ->
        DcLoadingBoxesItem(singleIncrement(index), item.barcode, item.dstFullAddress, false)
    }

    private val singleIncrement = { index: Int -> (index + 1).toString() }

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
        addSubscription(receptionInteractor.removeScannedBoxes(dcLoadingBoxes)
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
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getBoxDialogTitle(),
            message,
            resourceProvider.getBoxPositiveButton())
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

    object NavigateToBack

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}