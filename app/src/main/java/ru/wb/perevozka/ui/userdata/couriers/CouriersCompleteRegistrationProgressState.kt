package ru.wb.perevozka.ui.userdata.couriers


sealed class CouriersCompleteRegistrationProgressState {

    object Complete : CouriersCompleteRegistrationProgressState()
    object Progress : CouriersCompleteRegistrationProgressState()

}