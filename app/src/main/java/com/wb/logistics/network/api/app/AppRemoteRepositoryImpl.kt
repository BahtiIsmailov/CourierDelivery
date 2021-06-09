package com.wb.logistics.network.api.app

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.boxinfo.*
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightDstOfficeEntity
import com.wb.logistics.db.entity.flighboxes.FlightSrcOfficeEntity
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingDstOfficeEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingSrcOfficeEntity
import com.wb.logistics.network.api.app.remote.PutBoxCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.PutBoxFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxdeletefromflight.BoxDeleteFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxdeletefromflight.DeleteCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.remote.boxinfo.DstOfficeRemote
import com.wb.logistics.network.api.app.remote.boxinfo.SrcOfficeRemote
import com.wb.logistics.network.api.app.remote.flight.*
import com.wb.logistics.network.api.app.remote.flightboxtobalance.CurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxScannedRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.*
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Single

class AppRemoteRepositoryImpl(
    private val remote: AppApi,
    private val tokenManager: TokenManager,
) : AppRemoteRepository {

    override fun flight(): Single<FlightDataEntity> {
        return remote.flight(tokenManager.apiVersion())
            .map { FlightDataEntity(convertFlight(it), convertOffices(it.offices, it.id)) }
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
        offices.forEach { office ->
            LogUtils { logDebugApp(office.toString()) }
            officesEntity.add(with(office) {
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

    override fun warehouseBoxToBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.warehouseBoxToBalance(
            tokenManager.apiVersion(),
            flightID,
            FlightBoxScannedRemote(barcode,
                isManualInput,
                updatedAt,
                CurrentOfficeRemote(currentOffice)))
    }

    override fun pvzBoxToBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.pvzBoxToBalance(tokenManager.apiVersion(), flightID,
            FlightBoxScannedRemote(barcode,
                isManualInput,
                updatedAt,
                CurrentOfficeRemote(currentOffice)))
    }

    override fun removeBoxFromFlight(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        officeId: Int,
    ): Completable {
        return remote.deleteBoxFromFlight(tokenManager.apiVersion(), flightID,
            barcode,
            BoxDeleteFromFlightRemote(isManualInput,
                updatedAt,
                DeleteCurrentOfficeRemote(officeId)))
            .doOnError { LogUtils { logDebugApp(it.toString()) } }
    }

    override fun removeBoxFromBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.removeFromBalance(tokenManager.apiVersion(), flightID,
            barcode,
            PutBoxFromFlightRemote(isManualInput,
                updatedAt,
                PutBoxCurrentOfficeRemote(currentOffice)))
    }

    override fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>> {
        return remote.flightBoxes(tokenManager.apiVersion(), flightId)
            .map {
                val boxesEntity = mutableListOf<FlightBoxEntity>()
                it.data.forEach { box ->
                    boxesEntity.add(with(box) {
                        FlightBoxEntity(
                            barcode = barcode,
                            updatedAt = updatedAt,
                            status = status,
                            onBoard = onBoard,
                            srcOffice = FlightSrcOfficeEntity(
                                id = srcOffice.id,
                                name = srcOffice.name,
                                fullAddress = srcOffice.fullAddress,
                                longitude = srcOffice.long,
                                latitude = srcOffice.lat),
                            dstOffice = FlightDstOfficeEntity(
                                id = dstOffice.id,
                                name = dstOffice.name,
                                fullAddress = dstOffice.fullAddress,
                                longitude = dstOffice.long,
                                latitude = dstOffice.lat)
                        )
                    })
                }
                boxesEntity
            }
    }


    override fun time(): Single<TimeRemote> {
        return remote.getTime(tokenManager.apiVersion())
    }

    override fun boxInfo(barcode: String): Single<Optional<BoxInfoEntity>> {
        return remote.boxInfo(tokenManager.apiVersion(), barcode)
            .map { covertBoxInfoToFlight(it) }
            .map<Optional<BoxInfoEntity>> { Optional.Success(it) }
            .onErrorReturn { Optional.Empty() }
    }

    private fun convertBoxInfoDstOfficeEntity(dstOffice: DstOfficeRemote) =
        BoxInfoDstOfficeEntity(id = dstOffice.id,
            name = dstOffice.name,
            fullAddress = dstOffice.fullAddress,
            longitude = dstOffice.long,
            latitude = dstOffice.lat)

    private fun convertBoxInfoSrcOfficeEntity(srcOffice: SrcOfficeRemote) =
        with(srcOffice) {
            BoxInfoSrcOfficeEntity(
                id = id,
                name = name,
                fullAddress = fullAddress,
                longitude = long,
                latitude = lat)
        }

    override fun matchingBoxes(flightId: String): Single<List<MatchingBoxEntity>> {
        return remote.matchingBoxes(tokenManager.apiVersion(), flightId)
            .map {
                val matchingBoxesEntity = mutableListOf<MatchingBoxEntity>()
                it.data.forEach { box ->
                    matchingBoxesEntity.add(with(box) {
                        MatchingBoxEntity(
                            barcode = barcode,
                            srcOffice = MatchingSrcOfficeEntity(
                                id = srcOffice.id,
                                name = srcOffice.name,
                                fullAddress = srcOffice.fullAddress,
                                longitude = srcOffice.long,
                                latitude = srcOffice.lat),
                            dstOffice = MatchingDstOfficeEntity(
                                id = dstOffice.id,
                                name = dstOffice.name,
                                fullAddress = dstOffice.fullAddress,
                                longitude = dstOffice.long,
                                latitude = dstOffice.lat),
                        )
                    })
                }
                matchingBoxesEntity
            }
    }

    private fun covertBoxInfoToFlight(boxInfoRemote: BoxInfoRemote): BoxInfoEntity {
        return with(boxInfoRemote) {
            BoxInfoEntity(
                convertBoxEntity(box),
                convertBoxInfoFlightEntity()
            )
        }
    }

    private fun BoxInfoRemote.convertBoxEntity(boxRemote: com.wb.logistics.network.api.app.remote.boxinfo.BoxRemote): BoxEntity {
        return BoxEntity(
            barcode = box.barcode,
            srcOffice = convertBoxInfoSrcOfficeEntity(boxRemote.srcOffice),
            dstOffice = convertBoxInfoDstOfficeEntity(boxRemote.dstOffice),
            smID = box.smID)
    }

    private fun BoxInfoRemote.convertBoxInfoFlightEntity() =
        BoxInfoFlightEntity(
            id = flight.id,
            gate = flight.gate,
            plannedDate = flight.plannedDate,
            isAttached = flight.isAttached)

    override fun flightStatuses(): Single<FlightStatusesRemote> {
        return remote.flightStatuses(tokenManager.apiVersion())
    }

    override fun putFlightStatus(
        flightID: String,
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
        updatedAt: String,
    ): Completable {
        return remote.putFlightStatus(tokenManager.apiVersion(), flightID,
            StatusRemote(flightStatus.status, StatusLocationRemote(
                StatusOfficeLocationRemote(officeId), isGetFromGPS), updatedAt))
    }

    override fun getFlightStatus(flightID: String): Single<StatusStateEntity> {
        return remote.getFlightStatus(tokenManager.apiVersion(), flightID).map {
            with(it) {
                StatusStateEntity(
                    status = status,
                    location = StatusLocationEntity(
                        StatusOfficeLocationEntity(location.office.id), location.getFromGPS))
            }
        }
    }

}

data class FlightDataEntity(val flight: FlightEntity, val offices: List<FlightOfficeEntity>)