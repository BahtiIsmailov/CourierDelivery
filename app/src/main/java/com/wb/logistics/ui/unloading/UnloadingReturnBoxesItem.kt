package com.wb.logistics.ui.unloading

data class UnloadingReturnBoxesItem(
    val number: String,
    val barcode: String,
    val data: String,
    val isChecked: Boolean
)