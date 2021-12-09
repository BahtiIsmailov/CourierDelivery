package ru.wb.go.ui.auth.signup

class TimerOverStateImpl : TimerState {
    override fun handle(stateHandler: TimerStateHandler) {
        stateHandler.onTimeIsOverState()
    }
}