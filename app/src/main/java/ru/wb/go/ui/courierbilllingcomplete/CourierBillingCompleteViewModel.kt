package ru.wb.go.ui.courierbilllingcomplete

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel

class CourierBillingCompleteViewModel(
        parameters: CourierBillingCompleteParameters,
        resourceProvider: CourierBillingCompleteResourceProvider
) : NetworkViewModel() {

    private val _titleState = MutableLiveData<CourierBillingCompleteState>()
    val titleState: LiveData<CourierBillingCompleteState>
        get() = _titleState

    private val _navigateToBack = MutableLiveData<NavigateToBilling>()
    val navigateToBack: LiveData<NavigateToBilling>
        get() = _navigateToBack

    init {
        val amount = resourceProvider.getTitle(parameters.amount)
        onTechEventLog("init", "amount $amount")
        _titleState.value = CourierBillingCompleteState.InfoDelivery(amount)
    }

    fun onCompleteClick() {
        onTechEventLog("onCompleteClick", "NavigateToBilling")
        _navigateToBack.value = NavigateToBilling
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierCompleteDelivery"
    }

    object NavigateToBilling

}