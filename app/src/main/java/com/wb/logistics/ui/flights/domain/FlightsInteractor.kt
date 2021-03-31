package com.wb.logistics.ui.flights.domain

import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

interface FlightsInteractor {
    val action: BehaviorSubject<Boolean>
    fun flight(): Observable<FlightEntity<FlightsData>>
    fun changeBoxes(): Observable<List<ReceptionBoxEntity>>
    fun removeBoxes()
}