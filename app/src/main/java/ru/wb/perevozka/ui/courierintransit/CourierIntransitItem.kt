package ru.wb.perevozka.ui.courierintransit

data class CourierIntransitItem(
    val id: Int,
    val fullAddress: String,
    val deliveryCount: String,
    val isSelected: Boolean
)