package ru.wb.go.ui.courierintransit.delegates.items

data class CourierIntransitEmptyItem(
    val id: Int,
    val fullAddress: String,
    val timeWork:String,//+
    val deliveryCount: String,
    val fromCount: String,
    override var isSelected: Boolean,
    override var idView: Int,
) : BaseIntransitItem
