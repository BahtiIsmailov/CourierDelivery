package ru.wb.go.ui.courierdataexpects

import ru.wb.go.network.api.app.entity.CourierDocumentsEntity

sealed class CourierDataExpectsNavAction {

    object NavigateToCouriers : CourierDataExpectsNavAction()

    data class NavigateToDataType(val phone: String, val docs: CourierDocumentsEntity) :
        CourierDataExpectsNavAction()

}