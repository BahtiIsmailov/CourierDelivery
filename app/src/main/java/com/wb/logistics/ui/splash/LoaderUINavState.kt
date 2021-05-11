package com.wb.logistics.ui.splash

sealed class LoaderUINavState {
    object NavigateToApp : LoaderUINavState()
    object NavigateToNumberPhone : LoaderUINavState()
}