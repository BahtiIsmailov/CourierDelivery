package ru.wb.perevozka.network.api.app.remote

data class CarNumberRequest(
    val number: String,
    val isDefault: Boolean,
)