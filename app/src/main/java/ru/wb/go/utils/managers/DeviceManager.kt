package ru.wb.go.utils.managers

interface DeviceManager {
    fun guid(): String
    val deviceName: String
    val appVersion: String
    val appPackageName: String
    val versionOS: String
    val versionSDK: String
    val screenSize: Int

    val screenWidth: Int

    fun isAppVersionActual(adminVersion: String): Boolean
    var appAdminVersion: String
    val toolbarVersion: String
}