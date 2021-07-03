package com.wb.logistics.db.entity.attachedboxes

data class DeliveryBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val unloadedCount: Int,
    val attachedCount: Int,
    val returnCount: Int,

    )