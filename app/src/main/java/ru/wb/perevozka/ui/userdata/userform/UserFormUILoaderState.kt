package ru.wb.perevozka.ui.userdata.userform

sealed class UserFormUILoaderState {
    object Progress : UserFormUILoaderState()
    object Enable : UserFormUILoaderState()
    object Disable : UserFormUILoaderState()
}