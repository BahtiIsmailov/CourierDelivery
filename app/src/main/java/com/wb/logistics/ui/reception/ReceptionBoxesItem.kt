package com.wb.logistics.ui.reception

data class ReceptionBoxesItem(
    val number: String,
    val barcode: String,
    val address: String,
    val isChecked: Boolean
)