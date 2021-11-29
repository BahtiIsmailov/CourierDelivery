package ru.wb.go.network.exceptions

data class ApiErrorModel(val error: Error)

data class Error(
    val message: String,
    val code: String,
    val data: Data?,
)

data class Data(
    val ttl: Int,
)