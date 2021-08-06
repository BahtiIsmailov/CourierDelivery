package ru.wb.perevozka.db.entity.deliveryboxes

data class PickupPointBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val deliverCount: Int,
    val pickUpCount: Int,

    )