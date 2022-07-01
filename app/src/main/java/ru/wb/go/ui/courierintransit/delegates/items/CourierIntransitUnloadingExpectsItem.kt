package ru.wb.go.ui.courierintransit.delegates.items

data class CourierIntransitUnloadingExpectsItem(
    val id: Int,
    val fullAddress: String,
    val deliveryCount: String,
    val fromCount: String,
    val timeWork:String,
    override var isSelected: Boolean,
    override var idView: Int,
) : BaseIntransitItem
