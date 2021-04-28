package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import com.wb.logistics.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class UnloadingReturnBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val receptionInteractor: ReceptionInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _boxes = MutableLiveData<UnloadingReturnBoxesUIState<Nothing>>()
    val boxes: LiveData<UnloadingReturnBoxesUIState<Nothing>>
        get() = _boxes

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    private val _navigateToMessage = MutableLiveData<NavigateToMessage>()
    val navigateToMessage: LiveData<NavigateToMessage>
        get() = _navigateToMessage

    private val _enableRemove = MutableLiveData<Boolean>()
    val enableRemove: LiveData<Boolean>
        get() = _enableRemove

    private var copyReceptionBoxes = mutableListOf<UnloadingReturnBoxesItem>()

    init {
        val boxes = mutableListOf<UnloadingReturnBoxesItem>()
        for (i in 1 until 10) {
            val item = UnloadingReturnBoxesItem(
                i.toString(),
                "TRBX-11325658$i",
                "23.04.2021\u202220:0$i",
                false
            )
            boxes.add(item)
            changeBoxesComplete(boxes)
        }

//        addSubscription(receptionInteractor.observeScannedBoxes()
//            .flatMap { convertBoxes(it) }
//            .doOnNext { copyConvertBoxes(it) }
//            .subscribe({ changeBoxesComplete(it) },
//                { changeBoxesError(it) }))
    }

    private fun convertBoxes(boxes: List<AttachedBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()
            .toObservable()

    private val receptionBoxItem = { (index, item): IndexedValue<AttachedBoxEntity> ->
        UnloadingReturnBoxesItem(singleIncrement(index), item.barcode, item.dstFullAddress, false)
    }

    private val singleIncrement = { index: Int -> (index + 1).toString() }

    private fun copyConvertBoxes(boxes: List<UnloadingReturnBoxesItem>) {
        copyReceptionBoxes = boxes.toMutableList()
    }

    private fun changeBoxesComplete(boxes: List<UnloadingReturnBoxesItem>) {
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
        val checkedReceptionBoxes =
            copyReceptionBoxes.filter { it.isChecked }.map { it.barcode }.toMutableList()
        addSubscription(receptionInteractor.deleteScannedBoxes(checkedReceptionBoxes)
            .subscribe({
                _navigateToMessage.value = NavigateToMessage("Delete complete")
                _boxes.value = UnloadingReturnBoxesUIState.ProgressComplete
                _navigateToBack.value = NavigateToBack
            }, {
                _navigateToMessage.value = NavigateToMessage("Error delete: " + it.toString())
                _boxes.value = UnloadingReturnBoxesUIState.ProgressComplete
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
        _boxes.value = UnloadingReturnBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    private fun changeCheckedBox(index: Int, checked: Boolean) {
        val copyReception = copyReceptionBoxes[index].copy(isChecked = checked)
        copyReceptionBoxes[index] = copyReception
        _boxes.value = UnloadingReturnBoxesUIState.ReceptionBoxesItem(copyReceptionBoxes)
    }

    object NavigateToBack

    data class NavigateToMessage(val message: String)

}