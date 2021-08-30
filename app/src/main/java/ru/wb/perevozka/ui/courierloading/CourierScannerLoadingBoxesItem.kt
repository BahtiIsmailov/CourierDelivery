package ru.wb.perevozka.ui.courierloading

data class CourierScannerLoadingBoxesItem(
    val barcode: String,
    val unnamedBarcode: String,
    val address: String,
    val isChecked: Boolean
)