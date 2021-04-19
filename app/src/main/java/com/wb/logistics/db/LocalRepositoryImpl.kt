package com.wb.logistics.db

import com.wb.logistics.db.entity.boxtoflight.ScannedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxGroupByAddressEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalRepositoryImpl(
    private val flightDao: FlightDao,
    private val boxDao: BoxDao,
) : LocalRepository {

    override fun saveFlight(
        flightEntity: FlightEntity,
        flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable {
        return flightDao.insertFlight(flightEntity)
            .andThen(flightDao.insertFlightOffice(flightOfficesEntity))
    }

    override fun observeFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return flightDao.observeFlight().map { convertFlight(it) }
    }

    override fun readFlight(): Single<SuccessOrEmptyData<FlightEntity>> {
        return flightDao.readFlight()
            .map<SuccessOrEmptyData<FlightEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun readFlightData(): Single<SuccessOrEmptyData<FlightData>> {
        return flightDao.readFlightData().map { convertFlight(it) }
    }

    private fun convertFlight(flightData: FlightDataEntity?): SuccessOrEmptyData<FlightData> {
        return if (flightData == null) {
            SuccessOrEmptyData.Empty()
        } else {
            successFlightData(flightData)
        }
    }

    // TODO: 09.04.2021 вынести в конертер
    private fun successFlightData(flightDataEntity: FlightDataEntity): SuccessOrEmptyData<FlightData> {
        return with(flightDataEntity) {
            val addressesName = mutableListOf<String>()
            officeEntity.forEach { addresses -> addressesName.add(addresses.name) }
            SuccessOrEmptyData.Success(
                with(flightEntity) {
                    FlightData(
                        id,
                        gate,
                        plannedDate,
                        dc.name,
                        addressesName
                    )
                }
            )
        }
    }

    override fun deleteAllFlight() {
        flightDao.deleteAllFlight()
    }

    override fun saveFlightBoxes(boxesEntity: List<FlightBoxEntity>): Completable {
        return flightDao.insertFlightBoxes(boxesEntity)
    }

    override fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>> {
        return flightDao.findFlightBox(barcode)
            .map<SuccessOrEmptyData<FlightBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteAllFlightBoxes() {
        flightDao.deleteAllFlightBoxes()
    }

    //==============================================================================================
    override fun saveMatchingBoxes(matchingBoxes: List<MatchingBoxEntity>): Completable {
        return flightDao.insertMatchingBoxes(matchingBoxes)
    }

    override fun findMatchBox(barcode: String): Single<SuccessOrEmptyData<MatchingBoxEntity>> {
        return flightDao.findMatchingBox(barcode)
            .map<SuccessOrEmptyData<MatchingBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteAllMatchingBox() {
        flightDao.deleteAllMatchingBox()
    }

    //==============================================================================================
    //scanned box
    //==============================================================================================
    override fun saveFlightBoxScanned(flightBoxScannedEntity: ScannedBoxEntity): Completable {
        return boxDao.insertScannedBox(flightBoxScannedEntity)
    }

    override fun loadFlightBoxScanned(barcodes: List<String>): Single<List<ScannedBoxEntity>> {
        return boxDao.loadScannedBox(barcodes)
    }

    override fun observeFlightBoxScanned(): Flowable<List<ScannedBoxEntity>> {
        return boxDao.observeScannedBox()
    }

    override fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<ScannedBoxEntity>> {
        return boxDao.findScannedBox(barcode)
            .map<SuccessOrEmptyData<ScannedBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteFlightBoxScanned(flightBoxScannedEntity: ScannedBoxEntity): Completable {
        return boxDao.deleteScannedBox(flightBoxScannedEntity)
    }

    override fun deleteAllFlightBoxScanned() {
        boxDao.deleteAllScannedBox()
    }

    override fun groupByDstAddressScannedBoxScanned(): Single<List<ScannedBoxGroupByAddressEntity>> {
        return boxDao.groupByDstAddressScannedBox()
    }

    //==============================================================================================
    //balance box
    //==============================================================================================

    override fun saveFlightBoxBalanceAwait(flightBoxBalanceEntity: ScannedBoxBalanceAwaitEntity): Completable {
        return flightDao.insertFlightBoxBalanceAwait(flightBoxBalanceEntity)
    }

    override fun observeFlightBoxBalanceAwait(): Flowable<List<ScannedBoxBalanceAwaitEntity>> {
        return flightDao.observeFlightBoxBalanceAwait()
    }

    override fun flightBoxBalanceAwait(): Single<List<ScannedBoxBalanceAwaitEntity>> {
        return flightDao.flightBoxBalanceAwait()
    }

    override fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: ScannedBoxBalanceAwaitEntity): Completable {
        return flightDao.deleteFlightBoxBalanceAwait(flightBoxBalanceEntity)
    }

    override fun deleteAllFlightBoxBalanceAwait() {
        flightDao.deleteAllFlightBoxBalanceAwait()
    }

}