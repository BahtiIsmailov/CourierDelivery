package com.wb.logistics.ui.nav

import android.content.Context
import com.wb.logistics.R

class NavigationResourceProvider(private val context: Context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

}