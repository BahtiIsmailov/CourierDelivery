package com.wb.logistics.ui.unloadingforcedtermination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.monitor.NetworkState
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

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigateToMessageInfo = MutableLiveData<NavigateToMessageInfo>()
    val navigateToMessage: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    init {
        initTitle()
        observeNetworkState()
        initNotUnloadedBox()
    }

    private fun initTitle() {
        _boxesState.value = ForcedTerminationState.Title(resourceProvider.getLabel())
    }

    private fun initNotUnloadedBox() {
        addSubscription(interactor.observeNotUnloadedBoxBoxes(currentOffice())
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

    fun onCompleteClick() {
        val dataLog =
            resourceProvider.getDataLogFormat(currentOffice().toString(), resourceProvider.getBoxNotFound())
        addSubscription(interactor.completeUnloading(currentOffice(), dataLog)
            .subscribe(
                { _navigateToBack.value = ForcedTerminationNavAction.NavigateToFlightDeliveries },
                { completeUnloadingError(it) }))
    }

    private fun currentOffice() = parameters.currentOfficeId

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

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {}))
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}