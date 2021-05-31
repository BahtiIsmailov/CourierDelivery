package com.wb.logistics.ui.dcunloadingcongratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingCongratulationViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcUnloadingCongratulationResourceProvider,
    private val interactor: DcUnloadingCongratulationInteractor,
    private val screenManager: ScreenManager
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<InfoComplete>()
    val infoState: LiveData<InfoComplete>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToFlight>()
    val navigateToBack: LiveData<NavigateToFlight>
        get() = _navigateToBack

    init {
        addSubscription(interactor.congratulation().subscribe({
            with(it) {
                val delivered =
                    resourceProvider.getInfo(dcUnloadingCount, attachedCount + dcUnloadingCount)
                val returned = resourceProvider.getInfo(dcUnloadingReturnCount,
                    returnCount + dcUnloadingReturnCount)
                _infoState.value = InfoComplete(delivered, returned)
            }
        },
            {}))
    }

    fun onCompleteClick() {
        screenManager.clear()
        _navigateToBack.value = NavigateToFlight
    }

    data class InfoComplete(val deliveredCount: String, val returnCount: String)
    object NavigateToFlight

}