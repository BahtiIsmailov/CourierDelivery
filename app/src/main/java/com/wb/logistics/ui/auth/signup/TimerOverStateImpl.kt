package com.wb.logistics.ui.auth.signup

class TimerOverStateImpl : TimerState {
    override fun handle(stateHandler: TimerStateHandler) {
        stateHandler.onTimeIsOverState()
    }
}