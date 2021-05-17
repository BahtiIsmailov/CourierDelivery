package com.wb.logistics.network.api.app

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.LocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.boxinfo.*
import com.wb.logistics.db.entity.flight.*
import com.wb.logistics.db.entity.flightboxes.DstOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.SrcOfficeEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingDstOfficeEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingSrcOfficeEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.network.api.app.remote.PutBoxCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.PutBoxFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxdeletefromflight.BoxDeleteFromFlightRemote
import com.wb.logistics.network.api.app.remote.boxdeletefromflight.DeleteCurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.boxesfromflight.BoxRemote
import com.wb.logistics.network.api.app.remote.boxinfo.BoxInfoRemote
import com.wb.logistics.network.api.app.remote.boxinfo.DstOfficeRemote
import com.wb.logistics.network.api.app.remote.boxinfo.SrcOfficeRemote
import com.wb.logistics.network.api.app.remote.flight.*
import com.wb.logistics.network.api.app.remote.flightboxtobalance.CurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxScannedRemote
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class AppRepositoryImpl(
    private val remote: RemoteAppRepository,
    private val local: LocalRepository,
    private val timeManager: TimeManager,
) : AppRepository {

    override fun flightStatuses(): Single<FlightStatusesRemote> {
        return remote.flightStatuses()
    }

    override fun updateFlightAndTime(): Completable {
        val flight = remote.flight()
            .flatMapCompletable {
                local.saveFlight(
                    convertFlight(it),
                    convertOffices(it.offices, it.id))
            }
        val time = remote.getTime()
            .flatMapCompletable { Completable.fromAction { timeManager.saveNetworkTime(it.currentTime) } }
        return Completable.mergeArray(flight, time)
    }

    override fun updateFlightBoxes(flightId: Int): Completable {
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

    override fun deleteAllFlightData() {
        local.deleteAllFlight()
    }

    override fun changeFlightOfficeUnloading(
        dstOfficeId: Int,
        isUnloading: Boolean,
        notUnloadingCause: String,
    ): Completable {
        return local.changeFlightOfficeUnloading(dstOfficeId, isUnloading, notUnloadingCause)
    }

    override fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>> {
        return local.findFlightBox(barcode)
    }

    override fun deleteAllFlightBox() {
        local.deleteAllFlightBoxes()
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

    private fun convertOfficeLocation(officeLocation: OfficeLocationRemote?) =
        OfficeLocationEntity(officeLocation?.id ?: 0)

    override fun boxInfo(barcode: String): Single<SuccessOrEmptyData<BoxInfoEntity>> {
        return remote.boxInfo(barcode)
            .map { covertBoxInfoToFlight(it) }
            .map<SuccessOrEmptyData<BoxInfoEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun updateMatchingBoxes(flightId: String): Completable {
        return remote.matchingBoxes(flightId)
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
                            smID = smID)
                    })
                }
                matchingBoxesEntity
            }
            .flatMapCompletable { local.saveMatchingBoxes(it) }
    }

    override fun findMatchingBox(barcode: String): Single<SuccessOrEmptyData<MatchingBoxEntity>> {
        return local.findMatchBox(barcode)
    }

    override fun deleteAllMatchingBox() {
        local.deleteAllMatchingBox()
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

    private fun BoxInfoRemote.convertBoxEntity(boxRemote: com.wb.logistics.network.api.app.remote.boxinfo.BoxRemote): BoxEntity {
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
    //scanned box remote balance
    //==============================================================================================
    override fun loadBoxToBalanceRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.loadBoxToBalance(flightID,
            FlightBoxScannedRemote(barcode,
                isManualInput,
                updatedAt,
                CurrentOfficeRemote(currentOffice)))
    }

    override fun removeBoxFromFlightRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.deleteBoxFromFlight(flightID,
            barcode,
            BoxDeleteFromFlightRemote(isManualInput,
                updatedAt,
                DeleteCurrentOfficeRemote(currentOffice)))
            .doOnError { LogUtils { logDebugApp(it.toString()) } }
    }

    override fun removeBoxFromBalanceRemote(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.removeFromBalance(flightID,
            barcode,
            PutBoxFromFlightRemote(isManualInput,
                updatedAt,
                PutBoxCurrentOfficeRemote(currentOffice)))
    }

    //==============================================================================================
    //scanned box
    //==============================================================================================
    override fun saveAttachedBox(flightBoxScannedEntity: AttachedBoxEntity): Completable {
        return local.saveAttachedBox(flightBoxScannedEntity)
    }

    override fun observeAttachedBoxes(): Flowable<List<AttachedBoxEntity>> {
        return local.observeAttachedBoxes()
    }

    override fun observedAttachedBoxes(dstOfficeId: Int): Flowable<List<AttachedBoxEntity>> {
        return local.observeFilterByOfficeAttachedBoxes(dstOfficeId)
    }

    override fun readAllAttachedBoxes(): Single<List<AttachedBoxEntity>> {
        return local.readAttachedBoxes()
    }

    override fun deleteAttachedBox(flightBoxScannedEntity: AttachedBoxEntity): Completable {
        return local.deleteAttachedBox(flightBoxScannedEntity)
    }

    override fun deleteAllAttachedBoxes() {
        local.deleteAllAttachedBox()
    }

    override fun groupAttachedBoxesByDstAddress(): Single<List<AttachedBoxGroupByOfficeEntity>> {
        return local.groupAttachedBoxByDstAddress()
    }

    override fun groupAttachedBox(): Single<AttachedBoxResultEntity> {
        return local.groupAttachedBox()
    }

    override fun findAttachedBox(barcode: String): Single<SuccessOrEmptyData<AttachedBoxEntity>> {
        return local.findAttachedBox(barcode)
    }

    override fun findAttachedBoxes(barcodes: List<String>): Single<List<AttachedBoxEntity>> {
        return local.findAttachedBoxes(barcodes)
    }

    //==============================================================================================
    //unloaded
    //==============================================================================================
    override fun saveUnloadedBox(unloadedBoxEntity: UnloadedBoxEntity): Completable {
        return local.saveUnloadedBox(unloadedBoxEntity)
    }

    override fun observeUnloadedBoxes(): Flowable<List<UnloadedBoxEntity>> {
        return local.observeUnloadedBoxes()
    }

    override fun observeUnloadedBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<UnloadedBoxEntity>> {
        return local.observeUnloadedBoxesByDstOfficeId(dstOfficeId)
    }

    override fun findUnloadedBox(barcode: String): Single<SuccessOrEmptyData<UnloadedBoxEntity>> {
        return local.findUnloadedBox(barcode)
    }

    //==============================================================================================
    //balance await
    //==============================================================================================
    override fun saveFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: AttachedBoxBalanceAwaitEntity): Completable {
        return local.saveFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity)
    }

    override fun observeFlightBoxBalanceAwait(): Flowable<List<AttachedBoxBalanceAwaitEntity>> {
        return local.observeFlightBoxBalanceAwait()
    }

    override fun flightBoxBalanceAwait(): Single<List<AttachedBoxBalanceAwaitEntity>> {
        return local.flightBoxBalanceAwait()
    }

    override fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: AttachedBoxBalanceAwaitEntity): Completable {
        return local.deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity)
    }

    override fun deleteAllFlightBoxBalanceAwait() {
        local.deleteAllFlightBoxBalanceAwait()
    }

    //==============================================================================================
    //return
    //==============================================================================================

    override fun saveReturnBox(returnBoxEntity: ReturnBoxEntity): Completable {
        return local.saveReturnBox(returnBoxEntity)
    }

    override fun observedReturnBoxesByDstOfficeId(dstOfficeId: Int): Flowable<List<ReturnBoxEntity>> {
        return local.observedReturnBoxesByDstOfficeId(dstOfficeId)
    }

    override fun findReturnBox(barcode: String): Single<SuccessOrEmptyData<ReturnBoxEntity>> {
        return local.findReturnBox(barcode)
    }

    override fun findReturnBoxes(barcodes: List<String>): Single<List<ReturnBoxEntity>> {
        return local.findReturnBoxes(barcodes)
    }

    override fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity): Completable {
        return local.deleteReturnBox(returnBoxEntity)
    }

    //==============================================================================================
    //balance await
    //==============================================================================================

    override fun getOffsetLocalTime(): String {
        return timeManager.getOffsetLocalTime()
    }

}