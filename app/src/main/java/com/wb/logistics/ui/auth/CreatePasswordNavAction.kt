package com.wb.logistics.ui.auth

sealed class CreatePasswordNavAction {
    object NavigateToApplication : CreatePasswordNavAction()
    data class NavigateToTemporaryPassword(val phone: String) : CreatePasswordNavAction()
}