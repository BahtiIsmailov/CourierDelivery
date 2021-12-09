package ru.wb.go.ui.courierbillingaccountdata

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierBillingAccountDataResourceProvider(private val context: Context): BaseMessageResourceProvider(context) {

    fun getTitleCreate(): String {
        return context.getString(R.string.courier_billing_account_create_title)
    }

    fun getTitleEdit(): String {
        return context.getString(R.string.courier_billing_account_edit_title)
    }

}