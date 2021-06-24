package com.wb.logistics.db.entity.attachedboxes

data class DeliveryBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val attachedCount: Int,
    val returnCount: Int,
    val unloadedCount: Int,

    )