package com.wb.logistics.data

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.LocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxinfo.*
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.db.entity.flightboxes.DstOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.db.entity.flightboxes.SrcOfficeEntity
import com.wb.logistics.network.api.app.RemoteRepository
import com.wb.logistics.network.api.app.response.boxdeletefromflight.BoxDeletFromFlightRemote
import com.wb.logistics.network.api.app.response.boxdeletefromflight.DeleteCurrentOfficeRemote
import com.wb.logistics.network.api.app.response.boxesfromflight.BoxRemote
import com.wb.logistics.network.api.app.response.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.response.boxinfo.DstOfficeRemote
import com.wb.logistics.network.api.app.response.boxinfo.SrcOfficeRemote
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

    override fun updateFlight(): Completable {
        return remote.flight()
            .flatMapCompletable {
                local.saveFlight(
                    convertFlight(it),
                    convertOffices(it.offices, it.id))
            }
    }

    override fun updateFlightBox(flightId: Int): Completable {
        return remote.boxesFromFlight(flightId.toString())
            .map { it.data }
            .map { convertBox(it, flightId) }
            .flatMapCompletable { local.saveFlightBoxes(it) }
    }

    override fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return local.observeFlight()
    }

    override fun readFlight(): Single<SuccessOrEmptyData<FlightEntity>> {
        return local.readFlight()
    }

    override fun readFlightData(): Single<SuccessOrEmptyData<FlightData>> {
        return local.readFlightData()
    }

    override fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>> {
        return local.findBoxFromFlight(barcode)
    }

    private fun convertBox(boxes: List<BoxRemote>, flightId: Int): List<FlightBoxEntity> {
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

    override fun boxInfo(barcode: String): Single<SuccessOrEmptyData<BoxInfoEntity>> {
        return remote.boxInfo(barcode)
            .map { covertBoxInfoToFlight(it) }
            .map<SuccessOrEmptyData<BoxInfoEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    private fun covertBoxInfoToFlight(boxInfoRemote: BoxInfoRemote): BoxInfoEntity {
        return with(boxInfoRemote) {
            BoxInfoEntity(
                convertBoxEntity(box),
                convertBoxInfoFlightEntity()
            )
        }
    }

    private fun BoxInfoRemote.convertBoxInfoFlightEntity() =
        BoxInfoFlightEntity(
            id = flight.id,
            gate = flight.gate,
            plannedDate = flight.plannedDate,
            isAttached = flight.isAttached)

    private fun BoxInfoRemote.convertBoxEntity(boxRemote: com.wb.logistics.network.api.app.response.boxinfo.BoxRemote): BoxEntity {
        return BoxEntity(
            barcode = box.barcode,
            srcOffice = convertBoxInfoSrcOfficeEntity(boxRemote.srcOffice),
            dstOffice = convertBoxInfoDstOfficeEntity(boxRemote.dstOffice),
            smID = box.smID)
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


    //==============================================================================================
    override fun boxToFlight(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ): Completable {
        return remote.boxToFlight(flightID,
            BoxToFlightRemote(barcode, isManualInput, CurrentOfficeRemote(currentOffice)))
    }

    override fun boxDeleteOfFlight(
        flightID: String,
        barcode: String,
        isManual: Boolean,
        idOffice: Int,
    ): Completable {
        return remote.boxDeleteFromFlight(flightID,
            barcode,
            BoxDeletFromFlightRemote(isManual, DeleteCurrentOfficeRemote(idOffice)))
    }

    //==============================================================================================
    override fun saveFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable {
        return local.saveFlightBoxScanned(flightBoxScannedEntity)
    }

    override fun observeFlightBoxScanned(): Flowable<List<FlightBoxScannedEntity>> {
        return local.observeFlightBoxScanned()
    }

    override fun deleteAllFlightBoxScanned() {
        local.deleteAllFlightBoxScanned()
    }

    override fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<FlightBoxScannedEntity>> {
        return local.findFlightBoxScanned(barcode)
    }

}