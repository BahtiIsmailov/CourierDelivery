package ru.wb.perevozka.ui.auth.signup

interface TimerStateHandler {
    fun onTimerState(duration: Int, downTickSec: Int)
    fun onTimeIsOverState()
}