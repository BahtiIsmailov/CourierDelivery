package ru.wb.perevozka.ui.auth.signup

interface TimerState {
    fun handle(stateHandler: TimerStateHandler)
}