package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class ReceptionBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val receptionInteractor: ReceptionInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _boxes = MutableLiveData<ReceptionBoxesUIState<Nothing>>()
    val boxes: LiveData<ReceptionBoxesUIState<Nothing>>
        get() = _boxes

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    private val _enableRemove = MutableLiveData<Boolean>()
    val enableRemove: LiveData<Boolean>
        get() = _enableRemove

    private var copyReceptionBoxes = mutableListOf<ReceptionBoxesItem>()

    init {
        addSubscription(receptionInteractor.observeScannedBoxes()
            .flatMap { convertBoxes(it) }
            .doOnNext { copyConvertBoxes(it) }
            .subscribe({ changeBoxesComplete(it) },
                { changeBoxesError(it) }))
    }

    private fun convertBoxes(boxes: List<ScannedBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()
            .toObservable()

    private val receptionBoxItem = { (index, item): IndexedValue<ScannedBoxEntity> ->
        ReceptionBoxesItem(singleIncrement(index), item.barcode, item.dstFullAddress, false)
    }

    private val singleIncrement = { index: Int -> (index + 1).toString() }

    private fun copyConvertBoxes(boxes: List<ReceptionBoxesItem>) {
        copyReceptionBoxes = boxes.toMutableList()
    }

    private fun changeBoxesComplete(boxes: List<ReceptionBoxesItem>) {
        if (boxes.isEmpty()) {
            _boxes.value = ReceptionBoxesUIState.Empty
        } else {
            _boxes.value = ReceptionBoxesUIState.ReceptionBoxesItem(boxes)
        }
    }

    private fun changeBoxesError(error: Throwable) {
        LogUtils { logDebugApp(error.toString()) }
    }

    fun onRemoveClick() {
        _boxes.value = ReceptionBoxesUIState.Progress
        val checkedReceptionBoxes =
            copyReceptionBoxes.filter { it.isChecked }.map { it.barcode }.toMutableList()
        addSubscription(receptionInteractor.deleteScannedBoxes(checkedReceptionBoxes)
            .subscribe({
                _boxes.value = ReceptionBoxesUIState.ProgressComplete
                _navigateToBack.value = NavigateToBack
            }, {
                _boxes.value = ReceptionBoxesUIState.ProgressComplete
                changeDisableAllCheckedBox()
            }))
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
        _boxes.value = ReceptionBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    private fun changeCheckedBox(index: Int, checked: Boolean) {
        val copyReception = copyReceptionBoxes[index].copy(isChecked = checked)
        copyReceptionBoxes[index] = copyReception
        _boxes.value = ReceptionBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    object NavigateToBack

}