package ru.wb.go.network.api.app.entity

data class ParsedScanBoxQrEntity(
    val boxId: String,
    val officeId: String,
    val isOk:Boolean,
)
