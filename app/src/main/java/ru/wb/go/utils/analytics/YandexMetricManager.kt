package ru.wb.go.utils.analytics

interface YandexMetricManager {
    fun onTechErrorLog(screen: String, method: String, message: String)
    fun onTechUIEventLog(screen: String, method: String, message: String)
}