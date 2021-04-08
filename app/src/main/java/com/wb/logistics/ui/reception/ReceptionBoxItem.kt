package com.wb.logistics.ui.reception

data class ReceptionBoxItem(
    val number: String,
    val barcode: String,
    val address: String,
    val isChecked: Boolean
)