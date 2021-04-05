package com.wb.logistics.db.entity.boxinfo

import androidx.room.Entity

@Entity
data class BoxInfoEntity(
    val box: BoxEntity,
    val flight: FlightEntity,
)