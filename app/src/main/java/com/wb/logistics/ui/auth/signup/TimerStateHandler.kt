package com.wb.logistics.ui.auth.signup

interface TimerStateHandler {
    fun onTimerState(duration: Int)
    fun onTimeIsOverState()
}