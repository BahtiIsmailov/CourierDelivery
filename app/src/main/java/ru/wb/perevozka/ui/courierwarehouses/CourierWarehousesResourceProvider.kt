package ru.wb.perevozka.ui.courierwarehouses

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider

class CourierWarehousesResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getEmptyList() =
        context.getString(R.string.courier_warehouse_dialog_empty_list)

    fun getSearchEmpty() =
        context.getString(R.string.courier_warehouse_search_empty)

    fun getDialogEmptyTitle() =
        context.getString(R.string.courier_warehouse_dialog_empty_title)
    fun getDialogEmptyMessage() =
        context.getString(R.string.courier_warehouse_dialog_empty_message)
    fun getDialogEmptyButton() =
        context.getString(R.string.courier_warehouse_dialog_empty_button)

}