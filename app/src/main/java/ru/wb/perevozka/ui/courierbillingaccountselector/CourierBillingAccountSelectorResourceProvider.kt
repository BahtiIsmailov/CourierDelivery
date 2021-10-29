package ru.wb.perevozka.ui.courierbillingaccountselector

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider

class CourierBillingAccountSelectorResourceProvider(private val context: Context): BaseMessageResourceProvider(context) {

    fun getBalance(balance: String): String {
        return context.getString(R.string.courier_billing_account_selector_balance, balance)
    }

    fun getTitle(): String {
        return context.getString(R.string.courier_billing_account_selector_title)
    }

}