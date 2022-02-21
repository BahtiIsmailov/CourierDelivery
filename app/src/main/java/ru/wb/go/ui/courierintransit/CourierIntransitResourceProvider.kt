package ru.wb.go.ui.courierintransit

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierIntransitResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getLabelId(id : String): String {
        return context.getString(R.string.courier_intransit_label_id, id)
    }

    fun getLabel(): String {
        return context.getString(R.string.courier_intransit_label)
    }


    fun getBoxCountAndTotal(unload: Int, total: Int): String {
        return context.getString(R.string.courier_intransit_count, unload, total)
    }

    fun getEmptyMapIcon() = R.drawable.ic_intransit_office_map_empty

    fun getEmptySelectedMapIcon() = R.drawable.ic_intransit_office_map_empty_select

    fun getFailedUndeliveredAllMapIcon() = R.drawable.ic_intransit_office_map_undelivered_all

    fun getFailedUndeliveredAllSelectedMapIcon() = R.drawable.ic_intransit_office_map_undelivered_all_select

    fun getUnloadingExpectsMapIcon() = R.drawable.ic_intransit_expects_office_map

    fun getUnloadingExpectsSelectedMapIcon() = R.drawable.ic_intransit_expects_office_map_selected

    fun getCompleteMapIcon() = R.drawable.ic_intransit_office_map_complete

    fun getCompleteSelectMapIcon() = R.drawable.ic_intransit_office_map_complete_select

}