package ru.wb.go.utils.analytics

interface YandexMetricManager {
    fun onTechErrorLog(screen: String, method: String, message: String)
    fun onTechEventLog(screen: String, method: String, message: String)
    fun onTechNetworkLog(method: String, message: String)
}