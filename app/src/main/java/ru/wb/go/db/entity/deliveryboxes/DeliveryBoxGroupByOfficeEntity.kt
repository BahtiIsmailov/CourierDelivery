package ru.wb.go.db.entity.deliveryboxes

data class DeliveryBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val deliverCount: Int,
    val returnCount: Int,
    val deliveredCount: Int,
    val returnedCount: Int,
    val visitedAt: String

    )