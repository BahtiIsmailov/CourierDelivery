package com.wb.logistics.ui.auth.signup

class TimerStateImpl(private val duration: Int) : TimerState {
    override fun handle(stateHandler: TimerStateHandler) {
        stateHandler.onTimerState(duration)
    }
}