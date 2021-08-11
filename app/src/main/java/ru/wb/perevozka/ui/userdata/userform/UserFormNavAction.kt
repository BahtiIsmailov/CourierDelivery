package ru.wb.perevozka.ui.userdata.userform

sealed class UserFormNavAction {
    data class NavigateToCouriersCompleteRegistration(val phone: String) : UserFormNavAction()
}