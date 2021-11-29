package ru.wb.go.ui.auth.signup

interface TimerState {
    fun handle(stateHandler: TimerStateHandler)
}