package ru.wb.go.ui.auth.signup

class TimerStateImpl(private val duration: Int, private val downTickSec: Int) : TimerState {
    override fun handle(stateHandler: TimerStateHandler) {
        stateHandler.onTimerState(duration, downTickSec)
    }
}