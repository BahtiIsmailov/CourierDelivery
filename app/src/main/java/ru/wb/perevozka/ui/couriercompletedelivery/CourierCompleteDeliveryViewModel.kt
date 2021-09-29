package ru.wb.perevozka.ui.couriercompletedelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor

class CourierCompleteDeliveryViewModel(
    parameters: CourierCompleteDeliveryParameters,
    compositeDisposable: CompositeDisposable,
    resourceProvider: CourierCompleteDeliveryResourceProvider,
    private val interactor: CourierCompleteDeliveryInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<CourierCompleteDeliveryState>()
    val infoState: LiveData<CourierCompleteDeliveryState>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    init {
        _infoState.value = CourierCompleteDeliveryState.InfoDelivery(
            resourceProvider.getAmountInfo(parameters.amount),
            resourceProvider.getDeliveredInfo(parameters.unloadedCount, parameters.fromCount)
        )
    }

    fun onCompleteDeliveryClick() {
        _navigateToBack.value = NavigateToWarehouse
    }

    object NavigateToWarehouse

}