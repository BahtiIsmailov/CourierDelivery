package ru.wb.go.ui.courierintransitofficescanner

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierIntransitOfficeScannerResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getLabel(): String {
        return context.getString(R.string.courier_intransit_office_scanner_title)
    }

}