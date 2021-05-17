package com.wb.logistics.db.entity.attachedboxes

data class AttachedBoxGroupByOfficeEntity(

    val officeName: String,
    val officeId: Int,
    val dstFullAddress: String,
    val attachedCount: Int,
    val returnCount: Int,
    val unloadedCount: Int,
    val isUnloading: Boolean,

    )