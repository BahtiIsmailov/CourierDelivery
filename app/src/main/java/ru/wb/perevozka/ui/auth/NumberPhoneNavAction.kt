package ru.wb.perevozka.ui.auth

sealed class NumberPhoneNavAction {
    data class NavigateToCheckPassword(val number: String, val ttl : Int) : NumberPhoneNavAction()
    object NavigateToConfig : NumberPhoneNavAction()
    data class NavigateToUserForm(val phone: String) : NumberPhoneNavAction()
    data class NavigateToCouriersCompleteRegistration(val phone: String) : NumberPhoneNavAction()
}