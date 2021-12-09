package ru.wb.go.network.api.app.remote

data class CarNumberRequest(
    val number: String,
    val isDefault: Boolean,
)