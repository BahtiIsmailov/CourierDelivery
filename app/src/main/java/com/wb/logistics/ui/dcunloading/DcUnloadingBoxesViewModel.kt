package com.wb.logistics.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.utils.time.TimeFormatType
import com.wb.logistics.utils.time.TimeFormatter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: DcUnloadingInteractor,
    private val timeFormatter: TimeFormatter,
) : NetworkViewModel(compositeDisposable) {

    private val _boxesState = MutableLiveData<DcUnloadingBoxesState>()
    val boxesState: LiveData<DcUnloadingBoxesState>
        get() = _boxesState

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _boxesState.value = DcUnloadingBoxesState.Title("Выгруженные коробки")
        fetchDcUnloadedListBoxes()
    }

    private fun fetchDcUnloadedListBoxes() {
        addSubscription(interactor.findDcUnloadedListBoxes()
            .flatMap { list ->
                Observable.fromIterable(list.withIndex())
                    .map {
                        val date = timeFormatter.dateTimeWithoutTimezoneFromString(it.value.updatedAt)
                        val time = timeFormatter.format(date, TimeFormatType.ONLY_TIME)
                        DcUnloadingBoxesItem("" + (it.index + 1) + ". " + it.value.barcode, time)}.toList()
            }
            .subscribe({
                _boxesState.value = if (it.isEmpty()) DcUnloadingBoxesState.BoxesEmpty
                else DcUnloadingBoxesState.BoxesComplete(it)
            }, {})
        )
    }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}