package com.wb.logistics.ui.flights.domain

import com.wb.logistics.data.AppRepository
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.api.BoxesRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class FlightsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRepository: AppRepository,
    private val boxesRepository: BoxesRepository,
) : FlightsInteractor {

    override var updateFlight = BehaviorSubject.create<Boolean>()

    override fun flight(): Completable {
        return Observable.merge(networkMonitor(), updateFlight)
            .filter { it }
            .switchMapCompletable { appRepository.updateFlightAndBox() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun readFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return appRepository.readFlight().compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    private fun networkMonitor() = networkMonitorRepository.isNetworkConnected()

    override fun changeBoxes(): Observable<List<ReceptionBoxEntity>> {
        return boxesRepository.changeBoxes().compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeBoxes() {
        boxesRepository.removeBoxes()
    }

}