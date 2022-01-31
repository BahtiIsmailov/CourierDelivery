package ru.wb.go.utils.managers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import ru.wb.go.utils.LogUtils
import java.util.*

class DeviceManagerImpl(private val context: Context) : DeviceManager {

    override fun guid() = UUID.randomUUID().toString()

    override val deviceName = String.format("%s %s", Build.MANUFACTURER, Build.DEVICE)

    override val appVersion: String
        get() {
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                LogUtils { logError(this, "Can't get application version") }
            }
            return packageInfo?.versionName ?: ""
        }

    override val appPackageName = context.packageName

    override val versionOS = Build.VERSION.RELEASE ?: ""

    override val versionSDK = Build.VERSION.SDK_INT.toString()

    override val screenSize: Int
        get() {
            val metrics = context.resources.displayMetrics.density
            return when {
                metrics <= 1 -> ScreenSizeType.MDPI
                metrics <= 1.5 -> ScreenSizeType.HDPI
                metrics <= 2 -> ScreenSizeType.XHDPI
                metrics <= 3 -> ScreenSizeType.XXHDPI
                else -> ScreenSizeType.XXXHDPI
            }
        }

    override val screenWidth: Int
        get() {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels.toFloat()
            return dpWidth.toInt()
        }

    override var appAdminVersion: String = ""

    override fun isAppVersionActual(adminVersion: String): Boolean {
        // FIXME: remove after front-update version
        val av = adminVersion.replace("v", "")
        appAdminVersion = av
        return appVersion >= av
    }

}