package ru.wb.go.ui.splash

sealed class AppLoaderNavigatioState {
    object NavigateToCourier : AppLoaderNavigatioState()
    object NavigateToAuth : AppLoaderNavigatioState()
}