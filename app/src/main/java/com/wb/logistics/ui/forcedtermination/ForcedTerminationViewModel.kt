package com.wb.logistics.ui.forcedtermination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.forcedtermination.domain.ForcedTerminationInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class ForcedTerminationViewModel(
    private val parameters: ForcedTerminationParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: ForcedTerminationResourceProvider,
    private val interactor: ForcedTerminationInteractor,
    private val dataBuilder: ForcedTerminationDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    private val _boxesState = MutableLiveData<ForcedTerminationState>()
    val boxesState: LiveData<ForcedTerminationState>
        get() = _boxesState

    private val _navigateToBack = MutableLiveData<ForcedTerminationNavAction>()
    val navigateToBack: LiveData<ForcedTerminationNavAction>
        get() = _navigateToBack

    init {

        _boxesState.value = ForcedTerminationState.Title(resourceProvider.getLabel())

        addSubscription(interactor.observeAttachedBoxes(parameters.dstOfficeId)
            .switchMap { list ->
                Observable.fromIterable(list.withIndex())
                    .map { dataBuilder.buildForcedTerminationItem(it) }.toList().toObservable()
            }
            .subscribe {
                _boxesState.value =
                    if (it.isEmpty()) ForcedTerminationState.BoxesEmpty
                    else ForcedTerminationState.BoxesComplete(
                        resourceProvider.getNotDeliveryTitle(it.size), it)
            })
    }


    fun onCompleteClick(idx: Int) {
        val cause = when (idx) {
            0 -> resourceProvider.getBoxNotFound()
            1 -> resourceProvider.getNotPickupPoint()
            else -> resourceProvider.getEmpty()
        }
        addSubscription(interactor.completeUnloading(parameters.dstOfficeId, cause)
            .subscribe(
                { _navigateToBack.value = ForcedTerminationNavAction.NavigateToFlightDeliveries },
                { _navigateToBack.value = ForcedTerminationNavAction.NavigateToBack }))
    }


}