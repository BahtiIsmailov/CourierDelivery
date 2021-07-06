package com.wb.logistics.db.entity.deliveryboxes

data class FlightPickupPointBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val deliverCount: Int,
    val pickUpCount: Int,

    )