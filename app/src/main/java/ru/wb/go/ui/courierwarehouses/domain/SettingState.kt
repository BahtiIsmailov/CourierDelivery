package ru.wb.go.ui.courierwarehouses.domain

sealed class SettingState {
    object On : SettingState()
    object Off : SettingState()
}
