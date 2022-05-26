package ru.wb.go.ui.courierorders

import androidx.annotation.DrawableRes

sealed class CourierOrderDetailsInfoUIState {

    data class InitOrderDetails(
        val carNumber: CarNumberState,
        @DrawableRes
        val carTypeIcon: Int,
        val isChangeCarNumber: Boolean,
        val itemId: String,
        val orderId: String,
        val cost: String,
        val cargo: String,
        val countPvz: String,
        val reserve: String,
    ) : CourierOrderDetailsInfoUIState()

}