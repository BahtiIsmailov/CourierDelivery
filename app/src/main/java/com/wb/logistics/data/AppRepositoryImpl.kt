package com.wb.logistics.data

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.LocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxesfromflight.DstOfficeEntity
import com.wb.logistics.db.entity.boxesfromflight.FlightBoxEntity
import com.wb.logistics.db.entity.boxesfromflight.SrcOfficeEntity
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.network.api.app.RemoteRepository
import com.wb.logistics.network.api.app.response.boxesfromflight.BoxRemote
import com.wb.logistics.network.api.app.response.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.response.boxtoflight.BoxToFlightRemote
import com.wb.logistics.network.api.app.response.boxtoflight.CurrentOfficeRemote
import com.wb.logistics.network.api.app.response.flight.*
import com.wb.logistics.network.api.app.response.flightstatuses.FlightStatusesRemote
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class AppRepositoryImpl(
    private val remote: RemoteRepository,
    private val local: LocalRepository,
) : AppRepository {

    override fun flightStatuses(): Single<FlightStatusesRemote> {
        return remote.flightStatuses()
    }

    override fun updateFlightAndBox(): Completable {
        var flightId = 0
        return remote.flight()
            .doOnSuccess { flightId = it?.id ?: 0 }
            .flatMapCompletable {
                local.saveFlight(
                    convertFlight(it),
                    convertOffices(it.offices, it.id))
            }
            .andThen(remote.boxesFromFlight(flightId.toString()))
            .map { it.data }
            .map { convertBox(it, flightId) }
            .flatMapCompletable { local.saveBoxesFromFlight(it) }
    }

    override fun readFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return local.readFlight()
    }

    private fun convertBox(boxes: List<BoxRemote>, flightId : Int) : List<FlightBoxEntity> {
        val boxesEntity = mutableListOf<FlightBoxEntity>()
        boxes.forEach { box ->
            boxesEntity.add(with(box) {
                FlightBoxEntity(
                    flightId = flightId,
                    barcode = barcode,
                    srcOffice = SrcOfficeEntity(srcOffice.id),
                    dstOffice = DstOfficeEntity(dstOffice.id),
                    smID = smID
                )
            })
        }
        return boxesEntity
    }

    private fun convertFlight(flightRemote: FlightRemote) = with(flightRemote) {
        FlightEntity(
            id = id,
            gate = gate,
            dc = convertDc(flightRemote.dc),
            driver = convertDriver(flightRemote.driver),
            route = convertRoute(flightRemote.route),
            car = convertCar(flightRemote.car),
            plannedDate = plannedDate,
            startedDate = startedDate,
            status = status,
            location = convertLocation(flightRemote.location)
        )
    }

    private fun convertOffices(offices: List<OfficeRemote>, flightId: Int): List<FlightOfficeEntity> {
        val officesEntity = mutableListOf<FlightOfficeEntity>()
        offices.forEach { offece ->
            officesEntity.add(with(offece) {
                FlightOfficeEntity(
                    id = id,
                    flightId = flightId,
                    name = name,
                    fullAddress = fullAddress,
                    longitude = long,
                    latitude = lat
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

    private fun convertLocation(location: LocationRemote): LocationEntity = with(location) {
        LocationEntity(office = OfficeLocationEntity(office.id), getFromGPS = getFromGPS)
    }

    override fun boxInfo(barcode: String): Single<BoxInfoRemote> {
        return remote.boxInfo(barcode)
    }

    override fun boxToFlight(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ): Completable {
        return remote.boxToFlight(flightID,
            BoxToFlightRemote(barcode, isManualInput, CurrentOfficeRemote(currentOffice)))
    }

}