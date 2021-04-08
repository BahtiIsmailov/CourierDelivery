package com.wb.logistics.ui.flights.domain

import com.wb.logistics.data.AppRepository
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class FlightsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRepository: AppRepository,
) : FlightsInteractor {

    private var updateFlight = BehaviorSubject.create<Boolean>()

    override fun updateFlight() {
        updateFlight.onNext(true)
    }

    override fun flight(): Completable {
        // TODO: 07.04.2021 включить после отладки
//        return Observable.merge(networkMonitor(), updateFlight)
        return updateFlight
            .filter { it }
            .switchMapCompletable { updateFlightAndBox() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun updateFlightAndBox(): Completable {
        return appRepository.updateFlight()
            .andThen(appRepository.readFlightData())
            .flatMapCompletable {
                if (it is SuccessOrEmptyData.Success) appRepository.updateFlightBox(it.data.flight)
                else Completable.complete()
            }
    }

    override fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return appRepository.observeFlight().compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    private fun networkMonitor() = networkMonitorRepository.isNetworkConnected()

    override fun observeFlightBoxScanned(): Flowable<Int> {
        return appRepository.observeFlightBoxScanned().map { it.size }
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun removeBoxesToFlight() {
        appRepository.deleteAllFlightBoxScanned()
    }

}