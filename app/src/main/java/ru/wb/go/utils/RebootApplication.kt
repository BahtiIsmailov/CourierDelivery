package ru.wb.go.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log


object RebootApplication {

    fun doRestart(c: Context?) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                val pm: PackageManager = c.packageManager
                //check if we got the PackageManager
                //create the intent with the default start activity for your application
                val mStartActivity = pm.getLaunchIntentForPackage(
                    c.packageName
                )
                if (mStartActivity != null) {
                    mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    //create a pending intent so the application is restarted after System.exit(0) was called.
                    // We use an AlarmManager to call this intent in 100ms
                    val mPendingIntentId = 223344
                    val mPendingIntent = PendingIntent
                        .getActivity(
                            c, mPendingIntentId, mStartActivity,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    val mgr = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mgr[AlarmManager.RTC, System.currentTimeMillis()] =
                        mPendingIntent
                    //kill the application
                    System.exit(0)
                } else {
                    Log.e(TAG, "Was not able to restart application, mStartActivity null")
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Was not able to restart application")
        }
    }
}