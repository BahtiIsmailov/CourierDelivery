package ru.wb.go.ui.courierintransit.delegates.items

data class CourierIntransitUndeliveredAllItem(
    val id: Int,
    val fullAddress: String,
    val deliveryCount: String,
    val fromCount: String,
    override var isSelected: Boolean,
    override var idView: Int,
) : BaseIntransitItem
