package com.wb.logistics.ui.unloadingboxes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.unloadingboxes.domain.UnloadingBoxesInteractor
import com.wb.logistics.ui.unloadingscan.UnloadingScanResourceProvider
import com.wb.logistics.utils.time.TimeFormatType
import com.wb.logistics.utils.time.TimeFormatter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class UnloadingBoxesViewModel(
    parameters: UnloadingBoxesParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: UnloadingScanResourceProvider,
    private val timeFormatter: TimeFormatter,
    interactor: UnloadingBoxesInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _boxesState = MutableLiveData<UnloadingBoxesState>()
    val boxesState: LiveData<UnloadingBoxesState>
        get() = _boxesState

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        addSubscription(interactor.observeUnloadedBoxes(parameters.currentOfficeId)
            .switchMap { list ->
                Observable.fromIterable(list.withIndex())
                    .map {
                        with(it) {
                            val date =
                                timeFormatter.dateTimeWithoutTimezoneFromString(value.updatedAt)
                            val timeFormat = resourceProvider.getBoxTimeAndTime(
                                timeFormatter.format(date, TimeFormatType.ONLY_DATE),
                                timeFormatter.format(date, TimeFormatType.ONLY_TIME))
                            UnloadingBoxesItem(
                                resourceProvider.getUnnamedBarcodeFormat(singleIncrement(index),
                                    value.barcode),
                                timeFormat)
                        }
                    }
                    .toList()
                    .toObservable()
            }
            .subscribe {
                _boxesState.value =
                    if (it.isEmpty()) UnloadingBoxesState.BoxesEmpty
                    else UnloadingBoxesState.BoxesComplete(it)
            })
    }

    private val singleIncrement = { index: Int -> index + 1 }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}