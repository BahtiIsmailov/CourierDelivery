package ru.wb.go.utils.managers

import androidx.annotation.DimenRes

interface MetricManager {
    fun convertDpToPixel(dp: Float): Float
    fun convertPixelsToDp(px: Float): Float
    fun convertDimenResToPixels(@DimenRes resId: Int): Int
    val screenWidth: Int
}