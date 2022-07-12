package ru.wb.go.db.entity.courierlocal

data class CourierTimerEntity(
    val route:String,
    val name: String,
    val orderId: Int,
    val price: Int,
    val boxesCount: Int,
    val volume: Int,
    val countPvz: Int,
    val gate: String,
    val reservedDuration: String,
    val reservedAt: String
)