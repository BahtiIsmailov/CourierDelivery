package ru.wb.go.db.entity.courierlocal

import androidx.room.Entity

@Entity
data class LocalLoadingBoxEntity(
    val address: String,
    val count: Int,
)
