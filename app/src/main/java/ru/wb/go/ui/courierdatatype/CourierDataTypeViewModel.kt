package ru.wb.go.ui.courierdatatype

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.utils.analytics.YandexMetricManager

class CourierDataTypeViewModel(
    private val parameters: CourierDataParameters,

    metric: YandexMetricManager,

    private val resourceProviderData: CourierDataTypeResourceProvider,
) : NetworkViewModel(metric) {

    private val _navAction = MutableLiveData<CourierDataTypeNavAction>()
    val navigationState: LiveData<CourierDataTypeNavAction>
        get() = _navAction

    private val _switchState = SingleLiveEvent<CourierDataTypeSwitchState>()
    val switchState: LiveData<CourierDataTypeSwitchState>
        get() = _switchState

    init {
        onTechEventLog("init")
        _switchState.value =
            when (parameters.docs.courierType.lowercase()) {
                resourceProviderData.getSelfEmployed().lowercase() ->
                    CourierDataTypeSwitchState.IsSelfEmployed
                resourceProviderData.getIp().lowercase() ->
                    CourierDataTypeSwitchState.IsIP
                else -> CourierDataTypeSwitchState.IsEmpty
            }
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

