package ru.wb.perevozka.ui.couriercompletedelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor

class CourierCompleteDeliveryViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierCompleteDeliveryResourceProvider,
    private val interactor: CourierCompleteDeliveryInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<CourierCompleteDeliveryState>()
    val infoState: LiveData<CourierCompleteDeliveryState>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    init {
        addSubscription(interactor.getCompleteDeliveryResult()
            .map {
                with(it) {
                    CourierCompleteDeliveryState.InfoDelivery(
                        resourceProvider.getAmountInfo(amount),
                        resourceProvider.getDeliveredInfo(unloadedCount, fromCount)
                    )
                }
            }
            .subscribe({ _infoState.value = it }) {})
    }

    fun onCompleteDeliveryClick() {
        _navigateToBack.value = NavigateToWarehouse
    }

    object NavigateToWarehouse

}