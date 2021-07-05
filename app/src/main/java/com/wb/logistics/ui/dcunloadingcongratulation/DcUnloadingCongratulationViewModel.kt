package com.wb.logistics.ui.dcunloadingcongratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcunloading.DcUnloadingScanViewModel
import com.wb.logistics.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingCongratulationViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcUnloadingCongratulationResourceProvider,
    private val interactor: DcUnloadingCongratulationInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<InfoComplete>()
    val infoState: LiveData<InfoComplete>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToFlight>()
    val navigateToBack: LiveData<NavigateToFlight>
        get() = _navigateToBack

    private val _navigateToMessageInfo =
        SingleLiveEvent<DcUnloadingScanViewModel.NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<DcUnloadingScanViewModel.NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    init {
        addSubscription(interactor.congratulation().subscribe(
            congratulationComplete(), { congratulationError(it) }))
    }

    private fun congratulationComplete(): (t: DcCongratulationEntity) -> Unit =
        {
            with(it) {
                val delivered =
                    resourceProvider.getInfo(dcUnloadingCount, unloadingCount + dcUnloadingCount)
                val returned = resourceProvider.getInfo(dcUnloadingReturnCount,
                    returnCount + dcUnloadingReturnCount)
                _infoState.value = InfoComplete(delivered, returned)
            }
        }

    private fun congratulationError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        _navigateToMessageInfo.value = DcUnloadingScanViewModel.NavigateToMessageInfo(
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton())
    }

    fun onCompleteClick() {
        screenManager.clear()
        _navigateToBack.value = NavigateToFlight
    }

    data class InfoComplete(val deliveredCount: String, val returnCount: String)
    object NavigateToFlight

}