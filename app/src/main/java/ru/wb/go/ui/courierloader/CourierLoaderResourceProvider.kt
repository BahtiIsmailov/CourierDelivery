package ru.wb.go.ui.courierloader

import android.content.Context
import com.google.firebase.crashlytics.internal.common.CommonUtils
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierLoaderResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun isRooted(): Boolean {
        return CommonUtils.isRooted(context)
    }

}



