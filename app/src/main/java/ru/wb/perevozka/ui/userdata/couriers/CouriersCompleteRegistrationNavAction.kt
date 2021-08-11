package ru.wb.perevozka.ui.userdata.couriers

sealed class CouriersCompleteRegistrationNavAction {

    object NavigateToApplication : CouriersCompleteRegistrationNavAction()
    object NavigateToCouriersDialog : CouriersCompleteRegistrationNavAction()
}