package com.wb.logistics.ui.auth

sealed class TemporaryPasswordNavAction {
    data class NavigateToCreatePassword(val phone: String, val tmpPassword: String) :
        TemporaryPasswordNavAction()
}