package com.wb.logistics.db.entity.dcunloadedboxes

data class DcUnloadingScanBoxEntity(

    val barcode: String = "",
    val dcUnloadingCount: Int,
    val attachedCount: Int,
    val returnCount: Int,

)