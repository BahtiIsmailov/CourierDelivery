package ru.wb.go.utils.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings.Secure
import ru.wb.go.utils.LogUtils
import kotlin.system.exitProcess

class DeviceManagerImpl(private val context: Context) : DeviceManager {

    override val deviceId = Secure.getString(context.contentResolver, Secure.ANDROID_ID) ?: ""

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

    override fun doRestart() {
        try {
            val mStartActivity =
                context.packageManager?.getLaunchIntentForPackage(context.packageName)
            mStartActivity?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val mPendingIntent = PendingIntent.getActivity(
                context, PENDING_INTENT_ID, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT
            )
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Thread.sleep(RESTART_DELAY_TIME)
            mgr[AlarmManager.RTC, System.currentTimeMillis() + SYSTEM_TIME_OFFSET] = mPendingIntent
            exitProcess(EXIT_STATUS_PROCESS)
        } catch (ex: Exception) {
            LogUtils { logError(this, "Was not able to restart application") }
        }
    }

    override val screenWidth: Int
        get() {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels.toFloat()
            return dpWidth.toInt()
        }

    companion object {
        private const val PENDING_INTENT_ID = 223344
        private const val SYSTEM_TIME_OFFSET = 10
        private const val RESTART_DELAY_TIME = 100L
        private const val EXIT_STATUS_PROCESS = 0
    }

}