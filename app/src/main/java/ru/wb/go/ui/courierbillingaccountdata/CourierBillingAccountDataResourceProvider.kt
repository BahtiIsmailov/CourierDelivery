package ru.wb.go.ui.courierbillingaccountdata

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierBillingAccountDataResourceProvider(private val context: Context): BaseServicesResourceProvider(context) {

    fun getTitleCreate(): String {
        return context.getString(R.string.courier_billing_account_create_title)
    }

    fun getTitleEdit(): String {
        return context.getString(R.string.courier_billing_account_edit_title)
    }

}