package com.wb.logistics.network.exceptions

data class
ApiErrorModel(val error: Error)

data class Error(
    val message: String,
    val code: String
)