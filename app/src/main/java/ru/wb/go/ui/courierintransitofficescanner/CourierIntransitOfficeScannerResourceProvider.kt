package ru.wb.go.ui.courierintransitofficescanner

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierIntransitOfficeScannerResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getLabel(): String {
        return context.getString(R.string.courier_intransit_office_scanner_title)
    }

}