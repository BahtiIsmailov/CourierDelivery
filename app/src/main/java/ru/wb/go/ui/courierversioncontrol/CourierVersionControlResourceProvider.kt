package ru.wb.go.ui.courierversioncontrol

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierVersionControlResourceProvider(private val context: Context) :
        BaseServicesResourceProvider(context) {

    fun getAvailableVersion(version: String): String {
        return context.getString(R.string.app_update_version_available, version)
    }

    fun getUriPlayMarket(packageName: String): String {
        return context.getString(R.string.splash_uri_play_market, packageName)
                .replace(".debug", "")
    }

    fun getUriGoogle(packageName: String): String {
        return context.getString(R.string.splash_uri_google, packageName)
                .replace(".debug", "")
    }

}