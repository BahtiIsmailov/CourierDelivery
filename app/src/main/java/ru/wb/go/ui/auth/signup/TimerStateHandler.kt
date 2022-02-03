package ru.wb.go.ui.auth.signup

interface TimerStateHandler {
    fun onTimerState(duration: Int, downTickSec: Int)
    fun onTimeIsOverState()
}