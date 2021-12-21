package ru.wb.go.ui.courierversioncontrol

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierVersionControlResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getUriPlayMarket(packageName: String): String {
        return context.getString(R.string.splash_uri_play_market, packageName)
    }

    fun getUriGoogle(packageName: String): String {
        return context.getString(R.string.splash_uri_google, packageName)
    }

}