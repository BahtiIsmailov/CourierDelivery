package com.wb.logistics.db.entity.returnboxes

@Deprecated("")
data class ReturnBoxByDstOfficeIdEntity(

    val dstOfficeId: Int,
    val dstFullAddress: String,
    val count: Int,

    )