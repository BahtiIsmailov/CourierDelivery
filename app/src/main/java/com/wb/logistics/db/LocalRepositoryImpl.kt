package com.wb.logistics.db

import com.wb.logistics.db.entity.boxtoflight.FlightBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
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

    override fun removeFlight() {
        // TODO: 09.04.2021 реализовать
    }

    override fun saveFlightBoxes(boxesEntity: List<FlightBoxEntity>): Completable {
        return flightDao.insertFlightBoxes(boxesEntity)
    }

    override fun findFlightBox(barcode: String): Single<SuccessOrEmptyData<FlightBoxEntity>> {
        return flightDao.findFlightBox(barcode)
            .map<SuccessOrEmptyData<FlightBoxEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun removeFlightBoxes() {
        // TODO: 09.04.2021 реализовать
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

    //==============================================================================================
    //scanned box
    //==============================================================================================
    override fun saveFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable {
        return boxDao.insertFlightBoxScanned(flightBoxScannedEntity)
    }

    override fun loadFlightBoxScanned(barcodes: List<String>): Single<List<FlightBoxScannedEntity>> {
        return boxDao.loadFlightBoxScanned(barcodes)
    }

    override fun observeFlightBoxScanned(): Flowable<List<FlightBoxScannedEntity>> {
        return boxDao.observeFlightBoxScanned()
    }

    override fun findFlightBoxScanned(barcode: String): Single<SuccessOrEmptyData<FlightBoxScannedEntity>> {
        return boxDao.findFlightBoxScanned(barcode)
            .map<SuccessOrEmptyData<FlightBoxScannedEntity>> { SuccessOrEmptyData.Success(it) }
            .onErrorReturn { SuccessOrEmptyData.Empty() }
    }

    override fun deleteAllFlightBoxScanned() {
        boxDao.deleteAllFlightBoxScanned()
    }

    override fun deleteFlightBoxScanned(flightBoxScannedEntity: FlightBoxScannedEntity): Completable {
        return boxDao.deleteFlightBoxScanned(flightBoxScannedEntity)
    }

    //==============================================================================================
    //balance box
    //==============================================================================================

    override fun saveFlightBoxBalanceAwait(flightBoxBalanceEntity: FlightBoxBalanceAwaitEntity): Completable {
        return flightDao.insertFlightBoxBalanceAwait(flightBoxBalanceEntity)
    }

    override fun observeFlightBoxBalanceAwait(): Flowable<List<FlightBoxBalanceAwaitEntity>> {
        return flightDao.observeFlightBoxBalanceAwait()
    }

    override fun flightBoxBalanceAwait(): Single<List<FlightBoxBalanceAwaitEntity>> {
        return flightDao.flightBoxBalanceAwait()
    }

    override fun deleteFlightBoxBalanceAwait(flightBoxBalanceEntity: FlightBoxBalanceAwaitEntity): Completable {
        return flightDao.deleteFlightBoxBalanceAwait(flightBoxBalanceEntity)
    }

}