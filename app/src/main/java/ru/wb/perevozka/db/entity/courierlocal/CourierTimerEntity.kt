package ru.wb.perevozka.db.entity.courierlocal

data class CourierTimerEntity(
    val name: String,
    val orderId: Int,
    val price: Int,
    val boxesCount: Int,
    val volume: Int,
    val countPvz: Int,
    val reservedDuration: String,
    val reservedAt: String
)