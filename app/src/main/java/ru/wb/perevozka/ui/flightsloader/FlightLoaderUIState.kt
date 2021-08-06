package ru.wb.perevozka.ui.flightsloader

import androidx.navigation.NavDirections

sealed class FlightLoaderUIState {

    object NotAssigned : FlightLoaderUIState()
    data class Error(val message: String) : FlightLoaderUIState()
    data class InTransit(val navDirections: NavDirections) : FlightLoaderUIState()
    object InitProgress : FlightLoaderUIState()
    object Progress : FlightLoaderUIState()

}