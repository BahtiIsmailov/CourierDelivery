package com.wb.logistics.ui.splash

sealed class CounterBoxesActionStatus {

    data class Accepted(val accepted: String, val debt: String) : CounterBoxesActionStatus()
    data class AcceptedDebt(val accepted: String, val debt: String) : CounterBoxesActionStatus()

}