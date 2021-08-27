package ru.wb.perevozka.ui.couriercarnumber

import ru.wb.perevozka.db.entity.courier.CourierOrderEntity

sealed class CourierCarNumberNavigationState {

    data class NavigateToTimer(val title: String, val order: CourierOrderEntity) :
        CourierCarNumberNavigationState()

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierCarNumberNavigationState()
}