package ru.wb.perevozka.utils.managers

interface DeviceManager {
    val deviceId: String
    val deviceName: String
    val appVersion: String
    val appPackageName: String
    val versionOS: String
    val versionSDK: String
    val screenSize: Int
    fun doRestart()
    val screenWidth: Int
}