package com.wb.logistics.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingBoxesViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: DcUnloadingInteractor,
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
                    .map { "" + (it.index + 1) + ". " + it.value.barcode }.toList()
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