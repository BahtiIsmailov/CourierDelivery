package ru.wb.go.ui.courierversioncontrol

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.courierversioncontrol.domain.CourierVersionControlInteractor
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager

class CourierVersionControlViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierVersionControlResourceProvider,
    private val interactor: CourierVersionControlInteractor,
    private val deviceManager: DeviceManager
) : NetworkViewModel(compositeDisposable, metric) {

//    private val _infoState = MutableLiveData<CourierVersionControlState>()
//    val infoState: LiveData<CourierVersionControlState>
//        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    private val _updateFromGooglePlay = MutableLiveData<UpdateFromGooglePlay>()
    val updateFromGooglePlay: LiveData<UpdateFromGooglePlay>
        get() = _updateFromGooglePlay

    private val _updateFromFtp = MutableLiveData<UpdateFromFtp>()
    val updateFromFtp: LiveData<UpdateFromFtp>
        get() = _updateFromFtp

    init {
//        _infoState.value = CourierVersionControlState.InfoDelivery(
//            resourceProvider.getAmountInfo(parameters.amount),
//            resourceProvider.getDeliveredInfo(parameters.unloadedCount, parameters.fromCount)
//        )
    }

    fun onUpdateClick() {
        onTechEventLog("onUpdateClick")
        val packageName: String = deviceManager.appPackageName
        _updateFromGooglePlay.value = UpdateFromGooglePlay(resourceProvider.getUriPlayMarket(packageName),
            resourceProvider.getUriGoogle(packageName))

    }

    object NavigateToWarehouse
    data class UpdateFromGooglePlay(val uriPlayMarket: String, val packageName: String)
    object UpdateFromFtp

    companion object {
        const val SCREEN_TAG = "CourierVersionControl"
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

}