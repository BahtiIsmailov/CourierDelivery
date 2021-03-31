package com.wb.logistics.ui.flights

sealed class FlightsUIBottomState {
    object ScanBox : FlightsUIBottomState()
    object ReturnBox : FlightsUIBottomState()
}
