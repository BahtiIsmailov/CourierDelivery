package com.wb.logistics.ui.splash

import android.content.Context
import com.wb.logistics.R

class AppResourceProvider(private val context: Context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

}