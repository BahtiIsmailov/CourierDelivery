package ru.wb.perevozka.ui.splash

import android.content.Context
import ru.wb.perevozka.R

class AppResourceProvider(private val context: Context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

    fun getCount(count: Int) = context.getString(R.string.count_boxes, count)
    fun getDeliveryId(id: String) = context.getString(R.string.delivery, id)

}