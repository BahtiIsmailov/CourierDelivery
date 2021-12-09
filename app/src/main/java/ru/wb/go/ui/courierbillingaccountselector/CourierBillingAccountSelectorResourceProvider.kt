package ru.wb.go.ui.courierbillingaccountselector

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierBillingAccountSelectorResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getFormatAccount(bankName: String, account: String): String {
        return context.getString(
            R.string.courier_billing_account_selector_format,
            bankName,
            account.takeLast(4)
        )
    }

    fun getBalance(balance: String): String {
        return context.getString(R.string.courier_billing_account_selector_balance, balance)
    }

    fun getTitle(): String {
        return context.getString(R.string.courier_billing_account_selector_title)
    }

    fun getWithdrawBalance(balance: String): String {
        return context.getString(R.string.courier_billing_account_selector_withdraw_balance, balance)
    }

    fun getWithdrawBalanceInit(): String {
        return context.getString(R.string.courier_billing_account_selector_withdraw_balance_init)
    }



}