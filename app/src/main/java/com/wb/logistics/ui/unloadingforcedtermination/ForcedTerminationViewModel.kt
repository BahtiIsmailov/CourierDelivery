package com.wb.logistics.ui.unloadingforcedtermination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.unloadingforcedtermination.domain.ForcedTerminationInteractor
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

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessage: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    init {
        initTitle()
        initAttachedBox()
    }

    private fun initTitle() {
        _boxesState.value = ForcedTerminationState.Title(resourceProvider.getLabel())
    }

    private fun initAttachedBox() {
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
        addSubscription(interactor.completeUnloading(
            resourceProvider.getDataLogFormat(parameters.dstOfficeId.toString(),
                getCauseMessage(idx)))
            .subscribe(
                { _navigateToBack.value = ForcedTerminationNavAction.NavigateToFlightDeliveries },
                { completeUnloadingError(it) }))
    }

    private fun completeUnloadingError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getErrorCompleteUnloading()
        }
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getBoxDialogTitle(),
            message,
            resourceProvider.getBoxPositiveButton())
        _navigateToBack.value = ForcedTerminationNavAction.NavigateToBack
    }

    private fun getCauseMessage(idx: Int) = when (idx) {
        0 -> resourceProvider.getBoxNotFound()
        1 -> resourceProvider.getNotPickupPoint()
        else -> resourceProvider.getEmpty()
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}