package ru.wb.go.utils

import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment

fun vibrateOnAction(context: android.content.Context, delay: Long){

    val vibratorService = getSystemService(
        context,
        Vibrator::class.java
    ) as Vibrator
    vibratorService.let {
        if (Build.VERSION.SDK_INT >= 26) {
            it.vibrate(
                VibrationEffect.createOneShot(
                    delay,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(delay)
        }
    }
}

fun Fragment.hasPermissions(vararg permissions: String): Boolean =
    permissions.all(::hasPermission)

const val VIBRATE_SCAN = 100L
const val VIBRATE_CLICK = 50L

fun Fragment.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}