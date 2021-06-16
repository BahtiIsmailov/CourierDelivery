package com.wb.logistics.network.api.app

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.flighboxes.BoxStatus
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightDstOfficeEntity
import com.wb.logistics.db.entity.flighboxes.FlightSrcOfficeEntity
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingDstOfficeEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingSrcOfficeEntity
import com.wb.logistics.network.api.app.entity.boxinfo.*
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanDstOfficeEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanSrcOfficeEntity
import com.wb.logistics.network.api.app.remote.PutBoxCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.PutBoxFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxinfo.*
import com.wb.logistics.network.api.app.remote.deleteboxesfromflight.DeleteBoxesCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.deleteboxesfromflight.DeleteBoxesFromFlightRemote
import com.wb.logistics.network.api.app.remote.deleteboxfromflight.DeleteBoxCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.deleteboxfromflight.DeleteBoxFromFlightRemote
import com.wb.logistics.network.api.app.remote.flight.*
import com.wb.logistics.network.api.app.remote.flightboxtobalance.CurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxScannedRemote
import com.wb.logistics.network.api.app.remote.flightsstatus.*
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import com.wb.logistics.network.api.app.remote.warehousescan.WarehouseScanRemote
import com.wb.logistics.network.api.app.remote.warehousescan.WarehouseScannedBoxCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.warehousescan.WarehouseScannedBoxRemote
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Single

class AppRemoteRepositoryImpl(
    private val remote: AppApi,
    private val tokenManager: TokenManager,
) : AppRemoteRepository {

    override fun flight(): Single<FlightDataEntity> {
        return remote.flight(token())
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

    override fun pvzBoxToBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable {
        return remote.pvzBoxToBalance(tokenManager.apiVersion(), flightId,
            FlightBoxScannedRemote(barcode,
                isManualInput,
                updatedAt,
                CurrentOfficeRemote(currentOfficeId)))
    }

    override fun removeBoxFromFlight(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        officeId: Int,
    ): Completable {
        return remote.deleteBoxFromFlight(token(), flightId,
            barcode,
            DeleteBoxFromFlightRemote(isManualInput,
                updatedAt,
                DeleteBoxCurrentOfficeRemote(officeId)))
            .doOnError { LogUtils { logDebugApp(it.toString()) } }
    }

    override fun removeBoxesFromFlight(
        flightId: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
        barcodes: List<String>,
    ): Completable {
        return remote.deleteBoxesFromFlight(tokenManager.apiVersion(),
            flightId,
            DeleteBoxesFromFlightRemote(
                isManualInput = isManualInput,
                updatedAt = updatedAt,
                currentOffice = DeleteBoxesCurrentOfficeRemote(currentOfficeId),
                barcodes = barcodes))
    }

    override fun removeBoxFromBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Completable {
        return remote.removeFromBalance(token(), flightId,
            barcode,
            PutBoxFromFlightRemote(isManualInput,
                updatedAt,
                PutBoxCurrentOfficeRemote(currentOfficeId)))
    }

    override fun flightBoxes(flightId: String): Single<List<FlightBoxEntity>> {
        return remote.flightBoxes(token(), flightId)
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
        return remote.getTime(token())
    }

    override fun boxInfo(barcode: String): Single<BoxInfoDataEntity> {
        return remote.boxInfo(token(), barcode).map { covertBoxInfoToFlight(it) }
    }

    private fun convertBoxInfoDstOfficeEntity(dstOffice: BoxInfoDstOfficeRemote) =
        BoxInfoDstOfficeEntity(id = dstOffice.id,
            name = dstOffice.name,
            fullAddress = dstOffice.fullAddress,
            longitude = dstOffice.long,
            latitude = dstOffice.lat)

    private fun convertBoxInfoSrcOfficeEntity(srcOffice: BoxInfoSrcOfficeRemote) =
        with(srcOffice) {
            BoxInfoSrcOfficeEntity(
                id = id,
                name = name,
                fullAddress = fullAddress,
                longitude = long,
                latitude = lat)
        }

    override fun matchingBoxes(flightId: String): Single<List<MatchingBoxEntity>> {
        return remote.matchingBoxes(token(), flightId)
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

    private fun covertBoxInfoToFlight(boxInfoRemote: BoxInfoRemote): BoxInfoDataEntity {
        val flight = boxInfoRemote.flight
        val box = boxInfoRemote.box
        if (flight == null && box != null) {
            val boxEntityOptional = Optional.Success(convertBoxInfoEntity(box))
            return BoxInfoDataEntity(boxEntityOptional, Optional.Empty())
        }

        if (flight != null && box != null)
            return BoxInfoDataEntity(Optional.Success(convertBoxInfoEntity(box)),
                Optional.Success(convertBoxInfoFlightEntity(flight)))
        return BoxInfoDataEntity(Optional.Empty(), Optional.Empty())
    }

    private fun convertBoxInfoEntity(boxInfoItemRemote: BoxInfoItemRemote) =
        with(boxInfoItemRemote) {
            BoxInfoEntity(
                barcode = barcode,
                srcOffice = convertBoxInfoSrcOfficeEntity(srcOffice),
                dstOffice = convertBoxInfoDstOfficeEntity(dstOffice),
                smID = smID)
        }

    private fun convertBoxInfoFlightEntity(boxInfoFlightRemote: BoxInfoFlightRemote) =
        with(boxInfoFlightRemote) {
            BoxInfoFlightEntity(
                id = id,
                gate = gate,
                plannedDate = plannedDate,
                isAttached = isAttached)
        }

    override fun flightStatuses(): Single<FlightStatusesRemote> {
        return remote.flightStatuses(token())
    }

    override fun putFlightStatus(
        flightId: String,
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
        updatedAt: String,
    ): Completable {
        return remote.putFlightStatus(token(), flightId,
            StatusRemote(flightStatus.status, StatusLocationRemote(
                StatusOfficeLocationRemote(officeId), isGetFromGPS), updatedAt))
    }

    override fun getFlightStatus(flightId: String): Single<StatusStateEntity> {
        return remote.getFlightStatus(token(), flightId).map {
            with(it) {
                StatusStateEntity(
                    status = status,
                    location = StatusLocationEntity(
                        StatusOfficeLocationEntity(location.office.id), location.getFromGPS))
            }
        }
    }

    override fun warehouseScan(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ): Single<WarehouseScanEntity> {
        return remote.warehouseScan(token(), flightId,
            WarehouseScannedBoxRemote(
                barcode = barcode,
                isManualInput = isManualInput,
                updatedAt = updatedAt,
                WarehouseScannedBoxCurrentOfficeRemote(currentOfficeId)))
            .map { convertWarehouseScannedBox(it) }
    }

    private fun convertWarehouseScannedBox(warehouseScanRemote: WarehouseScanRemote): WarehouseScanEntity {
        return with(warehouseScanRemote) {
            WarehouseScanEntity(
                srcOffice = WarehouseScanSrcOfficeEntity(
                    id = dstOffice.id,
                    name = dstOffice.name,
                    fullAddress = dstOffice.fullAddress,
                    longitude = dstOffice.long,
                    latitude = dstOffice.lat),
                dstOffice = WarehouseScanDstOfficeEntity(
                    id = dstOffice.id,
                    name = dstOffice.name,
                    fullAddress = dstOffice.fullAddress,
                    longitude = dstOffice.long,
                    latitude = dstOffice.lat),
                barcode = barcode,
                updatedAt = updatedAt,
                status = BoxStatus.values()[status])
        }
    }

    private fun token() = tokenManager.apiVersion()

}

data class FlightDataEntity(val flight: FlightEntity, val offices: List<FlightOfficeEntity>)