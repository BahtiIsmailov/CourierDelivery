package ru.wb.perevozka.ui.courierwarehouses

import android.content.Context
import ru.wb.perevozka.R

class CourierWarehousesResourceProvider(private val context: Context) {

    fun getWarehouseDialogTitle() = context.getString(R.string.courier_warehouse_dialog_title_error)

    fun getErrorWarehouseDialogMessage(): String =
        context.getString(R.string.courier_warehouse_dialog_error)

    fun getBoxPositiveButton() =
        context.getString(R.string.courier_warehouse_dialog_positive_button_error)

}