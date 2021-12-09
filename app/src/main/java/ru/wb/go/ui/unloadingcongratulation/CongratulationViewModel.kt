package ru.wb.go.ui.unloadingcongratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.unloadingcongratulation.domain.CongratulationInteractor
import io.reactivex.disposables.CompositeDisposable

class CongratulationViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CongratulationResourceProvider,
    interactor: CongratulationInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToDcUnload>()
    val navigateToBack: LiveData<NavigateToDcUnload>
        get() = _navigateToBack

    init {
        addSubscription(interactor.getDeliveryBoxesGroupByOffice()
            .map { with(it) { resourceProvider.getInfo(unloadedCount, attachedCount) } }
            .subscribe({ _infoState.value = it }) {})
    }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToDcUnload
    }

    object NavigateToDcUnload

}