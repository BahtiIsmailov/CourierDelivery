package com.wb.logistics.ui.flights.domain

sealed class FlightEntity<T> {
    class Success<T>(val data: T) : FlightEntity<T>()
    class Empty<T> : FlightEntity<T>()
}
