package ru.wb.go.db.entity.dcunloadedboxes

data class DcNotUnloadedBoxEntity(
    val barcode: String,
    val updatedAt: String,
    val srcFullAddress: String,
    val currentOffice: Int,
    val srcOffice: Int,
    )