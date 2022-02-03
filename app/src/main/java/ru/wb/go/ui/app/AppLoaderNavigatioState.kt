package ru.wb.go.ui.app

sealed class AppLoaderNavigatioState {
    object NavigateToCourier : AppLoaderNavigatioState()
    object NavigateToAuth : AppLoaderNavigatioState()
}