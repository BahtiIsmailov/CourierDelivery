package com.wb.logistics.db.entity.attachedboxes

data class AttachedBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val redoCount: Int,
    val undoCount: Int,

    )