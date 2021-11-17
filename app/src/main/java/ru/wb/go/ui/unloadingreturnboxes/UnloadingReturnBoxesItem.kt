package ru.wb.go.ui.unloadingreturnboxes

data class UnloadingReturnBoxesItem(
    val barcode: String,
    val unnamedBarcode: String,
    val data: String,
    val isChecked: Boolean
)