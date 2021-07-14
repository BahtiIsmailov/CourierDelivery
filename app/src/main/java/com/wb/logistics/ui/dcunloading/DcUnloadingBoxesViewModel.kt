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
    private val resourceProvider: DcUnloadingScanResourceProvider,
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
                        val date =
                            timeFormatter.dateTimeWithoutTimezoneFromString(it.value.updatedAt)
                        val dateFormat = resourceProvider.getBoxDateAndTime(
                            timeFormatter.format(date, TimeFormatType.ONLY_DATE),
                            timeFormatter.format(date, TimeFormatType.ONLY_TIME))
                        val indexAndBarcode =
                            resourceProvider.getIndexAndBarcode(it.index + 1, it.value.barcode)
                        DcUnloadingBoxesItem(indexAndBarcode, dateFormat)
                    }.toList()
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