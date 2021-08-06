package ru.wb.perevozka.network.api.app.entity.boxinfo

data class BoxInfoFlightEntity(
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)