package com.wb.logistics.ui.unloading

data class UnloadingReturnBoxesItem(
    val barcode: String,
    val unnamedBarcode: String,
    val data: String,
    val isChecked: Boolean
)