package ru.wb.perevozka.ui.auth.signup

class TimerStateImpl(private val duration: Int) : TimerState {
    override fun handle(stateHandler: TimerStateHandler) {
        stateHandler.onTimerState(duration)
    }
}