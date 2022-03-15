package ru.wb.go.ui.courierdatatype

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.utils.analytics.YandexMetricManager

class CourierDataTypeViewModel(
    private val parameters: CourierDataParameters,

    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,

    private val resourceProviderData: CourierDataTypeResourceProvider,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _navAction = MutableLiveData<CourierDataTypeNavAction>()
    val navigationState: LiveData<CourierDataTypeNavAction>
        get() = _navAction

    private val _switchState = SingleLiveEvent<CourierDataTypeSwitchState>()
    val switchState: LiveData<CourierDataTypeSwitchState>
        get() = _switchState

    init {
        onTechEventLog("init")
        if (parameters.docs.courierType == resourceProviderData.getSelfEmployed())
            _switchState.value = CourierDataTypeSwitchState.IsSelfEmployed
    }

    fun onUpdateStatusClick(isSelfEmployed: Boolean) {
        parameters.docs.courierType =
            if (isSelfEmployed) resourceProviderData.getSelfEmployed() else resourceProviderData.getIp()
        _navAction.value =
            CourierDataTypeNavAction.NavigateToCourierData(
                parameters.phone, parameters.docs
            )
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierDataType"
    }

}