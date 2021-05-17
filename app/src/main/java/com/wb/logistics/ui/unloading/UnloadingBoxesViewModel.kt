package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class UnloadingBoxesViewModel(
    private val parameters: UnloadingBoxesParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: UnloadingScanResourceProvider,
    private val interactor: UnloadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _boxesState = MutableLiveData<UnloadingBoxesState>()
    val boxesState: LiveData<UnloadingBoxesState>
        get() = _boxesState

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {

        _boxesState.value = UnloadingBoxesState.Title("Список для выгрузки")

        addSubscription(interactor.observeAttachedBoxes(parameters.dstOfficeId)
            .switchMap {
                Observable.fromIterable(it.withIndex())
                    .map { "" + (it.index + 1) + ". " + it.value.barcode }.toList().toObservable()
            }
            .subscribe {
                _boxesState.value = if (it.isEmpty()) UnloadingBoxesState.BoxesEmpty
                else UnloadingBoxesState.BoxesComplete(it)
            })
    }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}