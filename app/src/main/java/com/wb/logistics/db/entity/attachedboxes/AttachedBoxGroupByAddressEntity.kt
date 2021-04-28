package com.wb.logistics.db.entity.attachedboxes

data class AttachedBoxGroupByAddressEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val redoCount: Int,
    val undoCount: Int,

    )