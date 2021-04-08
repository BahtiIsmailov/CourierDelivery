package com.wb.logistics.db.entity.boxinfo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BoxInfoEntity(
    @Embedded
    val box: BoxEntity,
    @Embedded
    val flight: BoxInfoFlightEntity,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "box_info_id")
    var id: Int = 0
}