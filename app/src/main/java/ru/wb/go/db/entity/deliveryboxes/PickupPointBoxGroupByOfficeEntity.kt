package ru.wb.go.db.entity.deliveryboxes

data class PickupPointBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val deliverCount: Int,
    val pickUpCount: Int,

    )