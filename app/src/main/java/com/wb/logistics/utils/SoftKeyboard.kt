package com.wb.logistics.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

object SoftKeyboard {
    fun hideKeyBoard(activity: Activity) {
        val focusedView = activity.currentFocus
        val imm = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            focusedView.clearFocus()
        } else imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }
}