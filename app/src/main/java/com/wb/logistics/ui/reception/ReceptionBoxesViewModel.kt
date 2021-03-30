package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
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

    init {
        addSubscription(receptionInteractor.changeBoxes()
            .flatMap { convertBoxes(it) }
            .doOnNext { copyConvertBoxes(it) }
            .subscribe({ changeBoxesComplete(it) },
                { changeBoxesError(it) }))
    }

    private fun convertBoxes(boxes: List<ReceptionBoxEntity>) =
        Observable.fromIterable(boxes.withIndex())
            .map(receptionBoxItem)
            .toList()
            .toObservable()

    private val receptionBoxItem = { (index, item): IndexedValue<ReceptionBoxEntity> ->
        ReceptionBoxItem(singleIncrement(index),
            item.box,
            item.address,
            false)
    }

    private val singleIncrement = { index: Int -> (index + 1).toString() }

    private var copyCheckedBoxes = mutableListOf<Boolean>()

    private fun copyConvertBoxes(boxes: List<ReceptionBoxItem>) {
        copyCheckedBoxes = boxes.map { it.isChecked }.toMutableList()
    }

    private fun changeBoxesComplete(boxes: List<ReceptionBoxItem>) {
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
        receptionInteractor.removeBoxes(copyCheckedBoxes)
        _navigateToBack.value = NavigateToBack
    }

    fun onItemClick(index: Int, checked: Boolean) {
        copyCheckedBoxes[index] = checked
        var activeRemove = false
        copyCheckedBoxes.forEach {
            if (it) {
                activeRemove = it
                return@forEach
            }
        }
        _enableRemove.value = activeRemove
    }

    object NavigateToBack

}