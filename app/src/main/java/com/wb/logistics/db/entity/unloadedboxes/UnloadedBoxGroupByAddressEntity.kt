package com.wb.logistics.db.entity.unloadedboxes

data class UnloadedBoxGroupByAddressEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val count: Int,

    )