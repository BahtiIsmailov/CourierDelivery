package com.wb.logistics.network.api.app

import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.flighboxes.BoxStatus
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightDstOfficeEntity
import com.wb.logistics.db.entity.flighboxes.FlightSrcOfficeEntity
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingDstOfficeEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingSrcOfficeEntity
import com.wb.logistics.network.api.app.entity.boxinfo.*
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanDstOfficeEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanSrcOfficeEntity
import com.wb.logistics.network.api.app.remote.boxinfo.*
import com.wb.logistics.network.api.app.remote.deleteboxesfromflight.DeleteBoxesCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.deleteboxesfromflight.RemoveBoxesFromFlightRequest
import com.wb.logistics.network.api.app.remote.deleteboxfromflight.DeleteBoxCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.deleteboxfromflight.RemoveBoxFromFlightRequest
import com.wb.logistics.network.api.app.remote.flight.*
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxToBalanceCurrentOfficeRequest
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxToBalanceRequest
import com.wb.logistics.network.api.app.remote.flightsstatus.*
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesResponse
import com.wb.logistics.network.api.app.remote.time.TimeResponse
import com.wb.logistics.network.api.app.remote.warehouse.*
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

    private fun convertFlight(flightRemote: FlightResponse) = with(flightRemote) {
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
        offices: List<FlightOfficeResponse>,
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

    private fun convertDc(dc: FlightDcResponse): DcEntity = with(dc) {
        DcEntity(id = id,
            name = name,
            fullAddress = fullAddress,
            longitude = long,
            latitude = lat)
    }

    private fun convertDriver(driver: FlightDriverResponse): DriverEntity = with(driver) {
        DriverEntity(id = id, name = name, fullAddress = fullAddress)
    }

    private fun convertRoute(route: FlightRouteResponse?): RouteEntity? =
        if (route == null) null else with(route) {
            RouteEntity(id = id,
                changed = changed,
                name = name)
        }

    private fun convertCar(car: FlightCarResponse): CarEntity = with(car) {
        CarEntity(id = id, plateNumber = plateNumber)
    }

    private fun convertOfficeLocation(officeLocation: FlightOfficeLocationResponse?) =
        OfficeLocationEntity(officeLocation?.id ?: 0)

    private fun convertLocation(location: FlightLocationResponse?): LocationEntity =
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
            FlightBoxToBalanceRequest(barcode,
                isManualInput,
                updatedAt,
                FlightBoxToBalanceCurrentOfficeRequest(currentOfficeId)))
    }

    override fun removeBoxFromFlight(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        officeId: Int,
    ): Completable {
        return remote.removeBoxFromFlight(token(), flightId,
            barcode,
            RemoveBoxFromFlightRequest(isManualInput,
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
        return remote.removeBoxesFromFlight(tokenManager.apiVersion(),
            flightId,
            RemoveBoxesFromFlightRequest(
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
        return remote.removeBoxFromWarehouseBalance(token(), flightId,
            BoxFromWarehouseBalanceRequest(
                barcode,
                isManualInput,
                updatedAt,
                BoxFromWarehouseBalanceCurrentOfficeRequest(currentOfficeId))).toCompletable()
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


    override fun time(): Single<TimeResponse> {
        return remote.getTime(token())
    }

    override fun boxInfo(barcode: String): Single<BoxInfoDataEntity> {
        return remote.boxInfo(token(), barcode).map { covertBoxInfoToFlight(it) }
    }

    private fun convertBoxInfoDstOfficeEntity(dstOffice: BoxInfoDstOfficeResponse) =
        BoxInfoDstOfficeEntity(id = dstOffice.id,
            name = dstOffice.name,
            fullAddress = dstOffice.fullAddress,
            longitude = dstOffice.long,
            latitude = dstOffice.lat)

    private fun convertBoxInfoSrcOfficeEntity(srcOffice: BoxInfoSrcOfficeResponse) =
        with(srcOffice) {
            BoxInfoSrcOfficeEntity(
                id = id,
                name = name,
                fullAddress = fullAddress,
                longitude = long,
                latitude = lat)
        }

    override fun warehouseMatchingBoxes(flightId: String): Single<List<WarehouseMatchingBoxEntity>> {
        return remote.warehouseMatchingBoxes(token(), flightId)
            .map {
                val matchingBoxesEntity = mutableListOf<WarehouseMatchingBoxEntity>()
                it.data.forEach { box ->
                    matchingBoxesEntity.add(with(box) {
                        WarehouseMatchingBoxEntity(
                            barcode = barcode,
                            srcOffice = WarehouseMatchingSrcOfficeEntity(
                                id = srcOffice.id,
                                name = srcOffice.name,
                                fullAddress = srcOffice.fullAddress,
                                longitude = srcOffice.long,
                                latitude = srcOffice.lat),
                            dstOffice = WarehouseMatchingDstOfficeEntity(
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

    private fun covertBoxInfoToFlight(boxInfoRemote: BoxInfoResponse): BoxInfoDataEntity {
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

    private fun convertBoxInfoEntity(boxInfoItemRemote: BoxInfoItemResponse) =
        with(boxInfoItemRemote) {
            BoxInfoEntity(
                barcode = barcode,
                srcOffice = convertBoxInfoSrcOfficeEntity(srcOffice),
                dstOffice = convertBoxInfoDstOfficeEntity(dstOffice),
                smID = smID)
        }

    private fun convertBoxInfoFlightEntity(boxInfoFlightRemote: BoxInfoFlightResponse) =
        with(boxInfoFlightRemote) {
            BoxInfoFlightEntity(
                id = id,
                gate = gate,
                plannedDate = plannedDate,
                isAttached = isAttached)
        }

    override fun flightStatuses(): Single<FlightStatusesResponse> {
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
            StatusResponse(flightStatus.status, StatusLocationResponse(
                StatusOfficeLocationResponse(officeId), isGetFromGPS), updatedAt))
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
        return remote.addBoxToWarehouseBalance(token(), flightId,
            BoxToWarehouseBalanceRequest(
                barcode = barcode,
                isManualInput = isManualInput,
                updatedAt = updatedAt,
                BoxToWarehouseBalanceCurrentOfficeRequest(currentOfficeId)))
            .map { convertWarehouseScannedBox(it) }
    }

    private fun convertWarehouseScannedBox(warehouseScanRemote: BoxToWarehouseBalanceResponse): WarehouseScanEntity {
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