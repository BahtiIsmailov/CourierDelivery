package ru.wb.go.ui.courierorders

import androidx.annotation.DrawableRes

data class CourierOrderDetailsAddressItem(
    @DrawableRes
    var icon: Int,
    val fullAddress: String,
    var isSelected: Boolean,
    var isUnspentTimeWork: Boolean,
    var timeWork: String
)