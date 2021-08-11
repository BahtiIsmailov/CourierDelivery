package ru.wb.perevozka.ui.userdata.couriers

import android.content.Context
import ru.wb.perevozka.R

class CouriersCompleteRegistrationResourceProvider(private val context: Context) {

    fun getInfo(delivery: Int, from: Int): String =
        context.getString(R.string.congratulation_info_count, delivery, from)

}