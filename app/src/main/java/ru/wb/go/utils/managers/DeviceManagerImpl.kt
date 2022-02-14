package ru.wb.go.utils.managers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.prefs.SharedWorker
import java.util.*

class DeviceManagerImpl(private val context: Context,
                        private val worker: SharedWorker
) : DeviceManager {

    companion object {
        const val DEFAULT_MOSCOW_COORDINATE = "55.751244:37.618423"
    }

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

    override fun lastLocation(): String {
        return worker.load(AppPreffsKeys.USER_INN_KEY, DEFAULT_MOSCOW_COORDINATE)
    }

    override fun saveLocation(location: String) {
        worker.save(AppPreffsKeys.LOCATION_KEY, location)
    }

    override val toolbarVersion: String
        get() {
            return appVersion.replace(".\\d+$".toRegex(), "")
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
        var av = adminVersion.replace("\\D".toRegex(), "")
        if (av == "") av = "0"
        var apv = appVersion.replace("\\D".toRegex(), "")
        if (apv == "") apv = "0"
        appAdminVersion = adminVersion
        return apv.toInt() >= av.toInt()
    }

}