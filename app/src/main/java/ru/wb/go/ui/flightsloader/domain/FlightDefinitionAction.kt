package ru.wb.go.ui.flightsloader.domain

import androidx.navigation.NavDirections

sealed class FlightDefinitionAction {

    object FlightEmpty : FlightDefinitionAction()

    data class NavigateToDirections(val navDirections: NavDirections) : FlightDefinitionAction()

}