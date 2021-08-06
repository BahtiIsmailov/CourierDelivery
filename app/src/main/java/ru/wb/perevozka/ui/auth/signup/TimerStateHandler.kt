package ru.wb.perevozka.ui.auth.signup

interface TimerStateHandler {
    fun onTimerState(duration: Int)
    fun onTimeIsOverState()
}