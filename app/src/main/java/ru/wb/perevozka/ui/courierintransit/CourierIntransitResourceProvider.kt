package ru.wb.perevozka.ui.courierintransit

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierIntransitResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getLabel(): String {
        return context.getString(R.string.courier_intransit_label)
    }

    fun getBoxCountAndTotal(unload: Int, total: Int): String {
        return context.getString(R.string.courier_intransit_count, unload, total)
    }

}