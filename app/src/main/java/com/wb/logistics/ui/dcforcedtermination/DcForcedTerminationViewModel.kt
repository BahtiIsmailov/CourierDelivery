package com.wb.logistics.ui.dcforcedtermination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcforcedtermination.domain.DcForcedTerminationInteractor
import io.reactivex.disposables.CompositeDisposable

class DcForcedTerminationViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcForcedTerminationResourceProvider,
    private val interactor: DcForcedTerminationInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _boxesState = MutableLiveData<DcForcedTerminationState>()
    val boxesState: LiveData<DcForcedTerminationState>
        get() = _boxesState

    private val _navigateAction = SingleLiveEvent<DcForcedTerminationNavAction>()
    val navigateToBack: LiveData<DcForcedTerminationNavAction>
        get() = _navigateAction

    init {

        _boxesState.value = DcForcedTerminationState.Title(resourceProvider.getLabel())

        addSubscription(interactor.observeDcUnloadedBoxes()
            .subscribe {
                _boxesState.value =
                    DcForcedTerminationState.BoxesUnloadCount(
                        resourceProvider.getNotDeliveryTitle(it.attachedCount + it.returnCount))
            })
    }

    fun onDetailsClick() {
        _navigateAction.value = DcForcedTerminationNavAction.NavigateToDetails
    }

    fun onCompleteClick(idx: Int) {
        val cause = when (idx) {
            0 -> resourceProvider.getBoxNotFound()
            1 -> resourceProvider.getNotPickupPoint()
            else -> resourceProvider.getEmpty()
        }
        // TODO: 24.05.2021 изменить статус рейса

        _navigateAction.value = DcForcedTerminationNavAction.NavigateToCongratulation
    }


}