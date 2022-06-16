package ru.wb.go.ui.courierbilllingcomplete

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor
import ru.wb.go.utils.analytics.YandexMetricManager

class CourierBillingCompleteViewModel(
        parameters: CourierBillingCompleteParameters,
        compositeDisposable: CompositeDisposable,
        metric: YandexMetricManager,
        resourceProvider: CourierBillingCompleteResourceProvider
) : NetworkViewModel(compositeDisposable, metric) {

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