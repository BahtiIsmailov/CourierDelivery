package ru.wb.go.ui.courierloader

import ru.wb.go.ui.courierdata.CourierDataParameters

sealed class CourierLoaderNavigationState {
    data class NavigateToCourierDataType(val courierDataParameters: CourierDataParameters) :
        CourierLoaderNavigationState()

    data class NavigateToCouriersCompleteRegistration(val phone: String) :
        CourierLoaderNavigationState()

    object NavigateToCourierWarehouse : CourierLoaderNavigationState()
    object NavigateToTimer : CourierLoaderNavigationState()
    object NavigateToScanner : CourierLoaderNavigationState()
    object NavigateToIntransit : CourierLoaderNavigationState()
    object NavigateToAppUpdate : CourierLoaderNavigationState()
    object NavigateToPhone : CourierLoaderNavigationState()

}

