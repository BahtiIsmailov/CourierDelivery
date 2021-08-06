package ru.wb.perevozka.ui.splash

sealed class LoaderUINavState {
    object NavigateToApp : LoaderUINavState()
    object NavigateToNumberPhone : LoaderUINavState()
}