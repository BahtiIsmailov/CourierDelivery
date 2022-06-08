package ru.wb.go.ui.courierstartdelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.couriercompletedelivery.domain.CourierCompleteDeliveryInteractor
import ru.wb.go.utils.analytics.YandexMetricManager

class CourierStartDeliveryViewModel(
    parameters: CourierStartDeliveryParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    resourceProvider: CourierStartDeliveryResourceProvider,
    private val interactor: CourierCompleteDeliveryInteractor,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _infoState = MutableLiveData<CourierStartDeliveryState>()
    val infoState: LiveData<CourierStartDeliveryState>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    init {
        val amount = resourceProvider.getAmountInfo(parameters.amount)
        val score = resourceProvider.getDeliverLoadCountInfo(parameters.loadedCount)
        onTechEventLog("init", score)
        _infoState.postValue(CourierStartDeliveryState.InfoDelivery(amount, score))
    }

    fun onCompleteDeliveryClick() {
        _navigateToBack.postValue(NavigateToWarehouse)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierStartDelivery"
    }

    object NavigateToWarehouse

}