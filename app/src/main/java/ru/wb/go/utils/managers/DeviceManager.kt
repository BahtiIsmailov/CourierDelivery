package ru.wb.go.utils.managers

interface DeviceManager {
    val deviceName: String
    val appVersion: String
    val appPackageName: String
    val versionOS: String
    val versionSDK: String
    val screenSize: Int
    fun doRestart()
    val screenWidth: Int
}