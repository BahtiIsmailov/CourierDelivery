package com.wb.logistics.ui.dcunloadingforcedtermination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcunloadingforcedtermination.domain.DcForcedTerminationInteractor
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

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        _boxesState.value = DcForcedTerminationState.Title(resourceProvider.getLabel())
        addSubscription(interactor.observeNotDcUnloadedBoxes()
            .subscribe {
                _boxesState.value =
                    DcForcedTerminationState.BoxesUnloadCount(
                        resourceProvider.getNotDeliveryTitle(it))
            })
    }

    fun onDetailsClick() {
        _navigateAction.value = DcForcedTerminationNavAction.NavigateToDetails
    }

    fun onCompleteClick() {
        bottomProgressEvent.value = true
        addSubscription(interactor.switchScreenToClosed(resourceProvider.getDataLogFormat(
            resourceProvider.getBoxNotFound())).subscribe(
            {
                _navigateAction.value = DcForcedTerminationNavAction.NavigateToCongratulation
                bottomProgressEvent.value = false
            },
            {
                bottomProgressEvent.value = false
                switchScreenToClosedError(it)
            }))
    }

    private fun switchScreenToClosedError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getForcedDialogMessage()
        }
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getForcedDialogTitle(),
            message,
            resourceProvider.getForcedDialogButton())
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}