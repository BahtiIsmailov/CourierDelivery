package com.wb.logistics.ui.delivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery")
data class DeliveryEntity(
    @PrimaryKey val
    id: Long,
)
