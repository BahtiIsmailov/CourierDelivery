package com.wb.logistics.ui.flights.domain

import com.wb.logistics.network.api.BoxesRepository
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.api.app.response.Flight
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class FlightsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRepository: AppRepository,
    private val boxesRepository: BoxesRepository,
) : FlightsInteractor {

    override var action = BehaviorSubject.create<Boolean>()

    override fun flight(): Observable<FlightEntity<FlightsData>> {
        val connectionMonitor = networkMonitorRepository.isNetworkConnected()
        val repository = appRepository.flight()
            .map { if (it.flight == null) FlightEntity.Empty() else successFlight(it.flight) }
            .toObservable()
        return Observable.merge(connectionMonitor, action)
            .filter { it }
            .switchMap { repository }
    }

    override fun changeBoxes(): Observable<List<ReceptionBoxEntity>> {
        return boxesRepository.changeBoxes().compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeBoxes() {
        boxesRepository.removeBoxes()
    }

    private fun successFlight(flight: Flight): FlightEntity.Success<FlightsData> {
        return with(flight) {
            val addressesName = mutableListOf<String>()
            offices.forEach { addresses -> addressesName.add(addresses.name) }
            FlightEntity.Success(
                FlightsData(
                    id,
                    gate,
                    plannedDate,
                    dc.name,
                    addressesName
                )
            )
        }
    }

}