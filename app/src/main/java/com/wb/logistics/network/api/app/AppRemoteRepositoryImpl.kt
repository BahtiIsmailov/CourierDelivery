package com.wb.logistics.network.api.app

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxinfo.*
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
import com.wb.logistics.network.api.app.remote.flight.FlightRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.CurrentOfficeRemote
import com.wb.logistics.network.api.app.remote.flightboxtobalance.FlightBoxScannedRemote
import com.wb.logistics.network.api.app.remote.flightstatuses.FlightStatusesRemote
import com.wb.logistics.network.api.app.remote.time.TimeRemote
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Single

class AppRemoteRepositoryImpl(
    private val remote: AppApi,
) : AppRemoteRepository {

    override fun warehouseBoxToBalance(
        flightID: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ): Completable {
        return remote.warehouseBoxToBalance(flightID,
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
        return remote.pvzBoxToBalance(flightID,
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
        currentOffice: Int,
    ): Completable {
        return remote.deleteBoxFromFlight(flightID,
            barcode,
            BoxDeleteFromFlightRemote(isManualInput,
                updatedAt,
                DeleteCurrentOfficeRemote(currentOffice)))
            .doOnError { LogUtils { logDebugApp(it.toString()) } }
    }

    override fun removeBoxFromBalance(
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

    override fun flightStatuses(): Single<FlightStatusesRemote> {
        return remote.flightStatuses()
    }

    override fun flight(): Single<FlightRemote?> {
        return remote.flight()
    }

    override fun time(): Single<TimeRemote> {
        return remote.getTime()
    }

    override fun boxInfo(barcode: String): Single<SuccessOrEmptyData<BoxInfoEntity>> {
        return remote.boxInfo(barcode)
            .map { covertBoxInfoToFlight(it) }
            .map<SuccessOrEmptyData<BoxInfoEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
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

    override fun updateMatchingBoxes(flightId: String): Single<List<MatchingBoxEntity>> {
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
    }

    private fun covertBoxInfoToFlight(boxInfoRemote: BoxInfoRemote): BoxInfoEntity {
        return with(boxInfoRemote) {
            BoxInfoEntity(
                convertBoxEntity(box),
                convertBoxInfoFlightEntity()
            )
        }
    }

}