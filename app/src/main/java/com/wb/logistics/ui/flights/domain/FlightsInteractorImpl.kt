package com.wb.logistics.ui.flights.domain

import com.wb.logistics.data.AppRepository
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
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

    private var updateFlightAndBoxes = BehaviorSubject.create<Boolean>()

    override fun updateFlight() {
        updateFlightAndBoxes.onNext(true)
    }

    override fun flight(): Completable {
        // TODO: 07.04.2021 включить после отладки
//        return Observable.merge(networkMonitor(), updateFlight)
        return updateFlightAndBoxes
            .filter { it }
            .switchMapCompletable { updateFlightAndBoxes() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun updateFlightAndBoxes(): Completable {
        return appRepository.updateFlight()
            .andThen(appRepository.readFlightData())
            .flatMapCompletable {
                if (it is SuccessOrEmptyData.Success) appRepository.updateMatchingBoxes(it.data.flight.toString())
                else Completable.complete()
            }
    }

    override fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return appRepository.observeFlight().compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    private fun networkMonitor() = networkMonitorRepository.isNetworkConnected()

    override fun observeFlightBoxScanned(): Flowable<Int> {
        return appRepository.observeFlightBoxesScanned().map { it.size }
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteFlightBoxes(): Completable {
        return appRepository.observeFlightBoxesScanned()
            .toObservable()
            .flatMapIterable { it }
            .flatMapCompletable {
                deleteScannedFlightBoxRemote(it).andThen(deleteScannedFlightBoxLocal(it))
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun deleteScannedFlightBoxRemote(flightBoxScannedEntity: FlightBoxScannedEntity) =
        with(flightBoxScannedEntity) {
            appRepository.deleteFlightBoxScannedRemote(
                flightId.toString(),
                barcode,
                isManualInput,
                srcOffice.id)
        }

    private fun deleteScannedFlightBoxLocal(flightBoxScannedEntity: FlightBoxScannedEntity) =
        appRepository.deleteFlightBoxScanned(flightBoxScannedEntity).onErrorComplete()

}