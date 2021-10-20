package ru.wb.perevozka.ui.courierstartdelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor

class CourierStartDeliveryViewModel(
    parameters: CourierStartDeliveryParameters,
    compositeDisposable: CompositeDisposable,
    resourceProvider: CourierStartDeliveryResourceProvider,
    private val interactor: CourierCompleteDeliveryInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<CourierStartDeliveryState>()
    val infoState: LiveData<CourierStartDeliveryState>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    init {
        _infoState.value = CourierStartDeliveryState.InfoDelivery(
            resourceProvider.getAmountInfo(parameters.amount),
            resourceProvider.getDeliverLoadCountInfo(parameters.loadedCount)
        )
    }

    fun onCompleteDeliveryClick() {
        _navigateToBack.value = NavigateToWarehouse
    }

    object NavigateToWarehouse

}