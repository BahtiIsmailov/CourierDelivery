package com.wb.logistics.ui.splash

sealed class CounterBoxesActionStatus {

    data class Accepted(
        val acceptedBox: String,
        val returnBox: String,
        val deliveryBox: String,
        val debtBox: String,
    ) : CounterBoxesActionStatus()

    data class AcceptedDebt(
        val acceptedBox: String,
        val returnBox: String,
        val deliveryBox: String,
        val debtBox: String,
    ) : CounterBoxesActionStatus()

}