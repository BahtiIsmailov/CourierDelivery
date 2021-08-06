package ru.wb.perevozka.ui.flightsloader.domain

import androidx.navigation.NavDirections

sealed class FlightDefinitionAction {

    object FlightEmpty : FlightDefinitionAction()

    data class NavigateComplete(val navDirections: NavDirections) : FlightDefinitionAction()

}