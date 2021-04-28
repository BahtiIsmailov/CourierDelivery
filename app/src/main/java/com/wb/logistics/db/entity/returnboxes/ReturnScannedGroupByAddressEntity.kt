package com.wb.logistics.db.entity.returnboxes

data class ReturnScannedGroupByAddressEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val count: Int,

    )