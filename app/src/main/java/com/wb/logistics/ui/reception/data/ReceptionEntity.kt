package com.wb.logistics.ui.reception.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reception")
data class ReceptionEntity(
    @PrimaryKey val
    id: Long,
)
