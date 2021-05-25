package com.wb.logistics.ui.unloadingcongratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.splash.domain.ScreenManager
import com.wb.logistics.ui.splash.domain.ScreenManagerState
import com.wb.logistics.ui.unloadingcongratulation.domain.CongratulationInteractor
import io.reactivex.disposables.CompositeDisposable

class CongratulationViewModel(
    private val parameters: CongratulationParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CongratulationResourceProvider,
    private val interactor: CongratulationInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToDcUnload>()
    val navigateToBack: LiveData<NavigateToDcUnload>
        get() = _navigateToBack

    init {
        addSubscription(interactor.groupAttachedBox().subscribe({
            screenManager.saveScreenState(ScreenManagerState.DcUnloading)
            _infoState.value =
                resourceProvider.getInfo(it.unloadedCount, it.attachedCount + it.unloadedCount, it.pickPointCount)
        },
            {}))
    }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToDcUnload
    }

    object NavigateToDcUnload

}