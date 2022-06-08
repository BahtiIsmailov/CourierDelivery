package ru.wb.go.ui.courierorders

data class CourierOrderDetailsAddressItem(
    val fullAddress: String,
    var isSelected: Boolean,
    var isUnspentTimeWork: Boolean,
    var timeWork: String
)