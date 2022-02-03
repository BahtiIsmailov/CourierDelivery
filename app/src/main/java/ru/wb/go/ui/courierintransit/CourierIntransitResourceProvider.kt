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

    fun getEmptyMapIcon() = R.drawable.ic_unload_office_map_empty

    fun getEmptyMapSelectedIcon() = R.drawable.ic_unload_office_map_empty_select

    fun getCompleteMapIcon() = R.drawable.ic_unload_office_map_complete

    fun getCompleteMapSelectIcon() = R.drawable.ic_unload_office_map_complete_select

    fun getFailedMapIcon() = R.drawable.ic_unload_office_map_failed

    fun getFailedMapSelectedIcon() = R.drawable.ic_unload_office_map_failed_select

    fun getWaitMapIcon() = R.drawable.ic_unload_wait_office_map

    fun getWaitMapSelectedIcon() = R.drawable.ic_unload_wait_office_map_selected

}