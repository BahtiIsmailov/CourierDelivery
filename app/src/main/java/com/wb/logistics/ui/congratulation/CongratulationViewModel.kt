package com.wb.logistics.ui.congratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.congratulation.domain.CongratulationInteractor
import io.reactivex.disposables.CompositeDisposable

class CongratulationViewModel(
    private val parameters: CongratulationParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CongratulationResourceProvider,
    private val interactor: CongratulationInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        addSubscription(interactor.groupAttachedBox().subscribe({
            _infoState.value =
                resourceProvider.getInfo(it.unloadedCount, it.attachedCount + it.unloadedCount, it.pickPointCount)
        },
            {}))
    }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}