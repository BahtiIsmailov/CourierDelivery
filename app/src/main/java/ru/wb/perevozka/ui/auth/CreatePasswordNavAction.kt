package ru.wb.perevozka.ui.auth

sealed class CreatePasswordNavAction {
    object NavigateToApplication : CreatePasswordNavAction()
}