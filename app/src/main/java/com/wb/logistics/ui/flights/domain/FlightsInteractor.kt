package com.wb.logistics.ui.flights.domain

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

interface FlightsInteractor {
    val action : BehaviorSubject<Boolean>
    fun flight(): Observable<FlightEntity<FlightsData>>
}