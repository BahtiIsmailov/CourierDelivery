package ru.wb.perevozka.ui.courierversioncontrol

import android.content.Context
import ru.wb.perevozka.R

class CourierVersionControlResourceProvider(private val context: Context) {

    fun getUriPlayMarket(packageName: String): String {
        return context.getString(R.string.splash_uri_play_market, packageName)
    }

    fun getUriGoogle(packageName: String): String {
        return context.getString(R.string.splash_uri_google, packageName)
    }
}