package ru.wb.go.network.api.app.remote.boxinfo

data class BoxInfoFlightResponse(
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)