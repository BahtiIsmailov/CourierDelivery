package com.wb.logistics.ui.flights.domain

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

interface FlightsInteractor {
    val updateFlight: BehaviorSubject<Boolean>
    fun flight(): Completable
    fun readFlight(): Flowable<SuccessOrEmptyData<FlightData>>
    fun changeBoxes(): Observable<List<ReceptionBoxEntity>>
    fun removeBoxes()
}