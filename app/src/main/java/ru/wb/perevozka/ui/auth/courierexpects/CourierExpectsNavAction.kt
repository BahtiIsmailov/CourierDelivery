package ru.wb.perevozka.ui.auth.courierexpects

sealed class CourierExpectsNavAction {

    object NavigateToApplication : CourierExpectsNavAction()
    data class NavigateToCouriersDialog(
        val style: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierExpectsNavAction()
}