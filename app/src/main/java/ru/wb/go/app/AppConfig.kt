package ru.wb.go.app

object AppConfig {
    const val HTTP_CONNECT_TIMEOUT = 30000L
    const val HTTP_READ_TIMEOUT = 30000L
    const val HTTP_CALL_TIMEOUT = 0L //чтобы не выбило при ретрае рэфреша
    const val PATH_CONFIG = "configs/configparams.json"
}