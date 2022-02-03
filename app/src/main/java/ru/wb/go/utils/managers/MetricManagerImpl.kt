package ru.wb.go.utils.managers

import android.content.Context
import android.util.DisplayMetrics
import androidx.annotation.DimenRes

class MetricManagerImpl(private val context: Context) : MetricManager {

    override fun convertDpToPixel(dp: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    override fun convertPixelsToDp(px: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    override fun convertDimenResToPixels(@DimenRes resId: Int): Int {
        return context.resources.getDimensionPixelSize(resId)
    }

    override val screenWidth: Int
        get() {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels.toFloat()
            return dpWidth.toInt()
        }

}