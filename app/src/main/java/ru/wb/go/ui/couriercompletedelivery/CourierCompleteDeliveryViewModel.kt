package ru.wb.go.ui.couriercompletedelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel

class CourierCompleteDeliveryViewModel(
    parameters: CourierCompleteDeliveryParameters,
    resourceProvider: CourierCompleteDeliveryResourceProvider
) : NetworkViewModel() {

    private val _infoState = MutableLiveData<CourierCompleteDeliveryState>()
    val infoState: LiveData<CourierCompleteDeliveryState>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    init {
        val amount = resourceProvider.getAmountInfo(parameters.amount)
        val score = resourceProvider.getDeliveredInfo(parameters.unloadedCount, parameters.fromCount)
        //onTechEventLog("init", "score $amount")
        _infoState.value = CourierCompleteDeliveryState.InfoDelivery(amount, score)
    }

    fun onCompleteDeliveryClick() {
        //onTechEventLog("onCompleteDeliveryClick", "NavigateToWarehouse")
        _navigateToBack.value = NavigateToWarehouse
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierCompleteDelivery"
    }

    object NavigateToWarehouse


}