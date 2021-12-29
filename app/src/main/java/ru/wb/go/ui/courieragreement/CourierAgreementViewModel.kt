package ru.wb.go.ui.courieragreement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.analytics.YandexMetricManager

class CourierAgreementViewModel(compositeDisposable: CompositeDisposable, metric: YandexMetricManager) :
    NetworkViewModel(compositeDisposable, metric) {

    private val _navigationState = MutableLiveData<CourierAgreementNavigationState>()
    val navigationState: LiveData<CourierAgreementNavigationState>
        get() = _navigationState

    fun onCompleteClick() {
        onTechEventLog("onCompleteClick")
        _navigationState.value = CourierAgreementNavigationState.Complete
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierAgreement"
    }

}