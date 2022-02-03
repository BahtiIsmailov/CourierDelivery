package ru.wb.go.ui.courierexpects

import ru.wb.go.network.api.app.entity.CourierDocumentsEntity

sealed class CourierExpectsNavAction {

    object NavigateToCouriers : CourierExpectsNavAction()
    data class NavigateToRegistrationCouriers(val phone: String, val docs: CourierDocumentsEntity) :
        CourierExpectsNavAction()

}