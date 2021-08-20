package ru.wb.perevozka.ui.userdata.userform

sealed class UserFormUIAction {

    data class FocusChange(val text: String, val type: UserFormQueryType, val hasFocus: Boolean) :
        UserFormUIAction()

    data class TextChange(val text: String, val type: UserFormQueryType) : UserFormUIAction()
    data class CompleteClick(var userData: MutableList<UserData>) : UserFormUIAction()
}