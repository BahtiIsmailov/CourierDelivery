package ru.wb.perevozka.ui.courierexpects

sealed class CourierExpectsNavAction {

    object NavigateToCouriers : CourierExpectsNavAction()
    data class NavigateToCouriersDialog(
        val style: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierExpectsNavAction()
}