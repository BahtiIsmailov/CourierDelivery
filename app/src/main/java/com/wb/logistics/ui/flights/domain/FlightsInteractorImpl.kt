package com.wb.logistics.ui.flights.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.remote.flight.*
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class FlightsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
) : FlightsInteractor {

    private var updateFlightAndBoxes = BehaviorSubject.create<Boolean>()

    override fun updateFlight(): Completable {
        //updateFlightAndBoxes.onNext(true)
        return updateFlightAndBoxes()
    }

    // TODO: 07.04.2021 включить после отладки
//    override fun flight(): Completable {
////        return Observable.merge(networkMonitor(), updateFlight)
//        return updateFlightAndBoxes
//            .filter { it }
//            .switchMapCompletable { updateFlightAndBoxes() }
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())
//    }

    private fun updateFlightAndBoxes(): Completable {
        return updateFlightAndTime()
            .andThen(appLocalRepository.readFlightData())
            .flatMapCompletable {
                if (it is SuccessOrEmptyData.Success)
                    appRemoteRepository.updateMatchingBoxes(it.data.flight.toString())
                        .flatMapCompletable { appLocalRepository.saveMatchingBoxes(it) }
                else Completable.complete()
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun updateFlightAndTime(): Completable {
        val flight = appRemoteRepository.flight()
            .flatMapCompletable {
                appLocalRepository.saveFlight(
                    convertFlight(it),
                    convertOffices(it.offices, it.id))
            }
        val time = appRemoteRepository.time()
            .flatMapCompletable {
                Completable.fromAction { timeManager.saveNetworkTime(it.currentTime) }
            }
        return Completable.mergeArray(flight, time)
    }

    private fun convertFlight(flightRemote: FlightRemote) = with(flightRemote) {
        with(flightRemote) {
            FlightEntity(
                id = id,
                gate = gate,
                dc = convertDc(dc),
                driver = convertDriver(driver),
                route = convertRoute(route),
                car = convertCar(car),
                plannedDate = plannedDate,
                startedDate = startedDate ?: "",
                status = status,
                location = convertLocation(location)
            )
        }
    }

    private fun convertOffices(
        offices: List<OfficeRemote>,
        flightId: Int,
    ): List<FlightOfficeEntity> {
        val officesEntity = mutableListOf<FlightOfficeEntity>()
        offices.forEach { offece ->
            officesEntity.add(with(offece) {
                FlightOfficeEntity(
                    id = id,
                    flightId = flightId,
                    name = name,
                    fullAddress = fullAddress,
                    longitude = long,
                    latitude = lat,
                    isUnloading = false,
                    notUnloadingCause = ""
                )
            })
        }
        return officesEntity
    }

    private fun convertDc(dc: DcRemote): DcEntity = with(dc) {
        DcEntity(id = id,
            name = name,
            fullAddress = fullAddress,
            longitude = long,
            latitude = lat)
    }

    private fun convertDriver(driver: DriverRemote): DriverEntity = with(driver) {
        DriverEntity(id = id, name = name, fullAddress = fullAddress)
    }

    private fun convertRoute(route: RouteRemote?): RouteEntity? =
        if (route == null) null else with(route) {
            RouteEntity(id = id,
                changed = changed,
                name = name)
        }

    private fun convertCar(car: CarRemote): CarEntity = with(car) {
        CarEntity(id = id, plateNumber = plateNumber)
    }

    private fun convertOfficeLocation(officeLocation: OfficeLocationRemote?) =
        OfficeLocationEntity(officeLocation?.id ?: 0)

    private fun convertLocation(location: LocationRemote?): LocationEntity =
        with(location) {
            if (location == null) {
                LocationEntity(office = OfficeLocationEntity(0),
                    getFromGPS = false)
            } else {
                LocationEntity(office = convertOfficeLocation(this?.office),
                    getFromGPS = this?.getFromGPS ?: false)
            }
        }

    override fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return appLocalRepository.observeFlight()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    private fun networkMonitor() = networkMonitorRepository.isNetworkConnected()

    override fun observeFlightBoxScanned(): Flowable<Int> {
        return appLocalRepository.observeAttachedBoxes().map { it.size }
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteFlightBoxes(): Completable {
        return appLocalRepository.observeAttachedBoxes()
            .toObservable()
            .flatMapIterable { it }
            .flatMapCompletable {
                deleteScannedFlightBoxRemote(it).andThen(deleteScannedFlightBoxLocal(it))
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun deleteScannedFlightBoxRemote(attachedBoxEntity: AttachedBoxEntity) =
        with(attachedBoxEntity) {
            appRemoteRepository.removeBoxFromFlight(
                flightId.toString(),
                barcode,
                isManualInput,
                updatedAt,
                srcOffice.id)
        }

    private fun deleteScannedFlightBoxLocal(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.deleteAttachedBox(attachedBoxEntity).onErrorComplete()

}