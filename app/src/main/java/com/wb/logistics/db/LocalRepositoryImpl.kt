package com.wb.logistics.db

import com.wb.logistics.db.entity.boxesfromflight.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightDataEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalRepositoryImpl(
    private val flightDao: FlightDao,
) : LocalRepository {

    override fun saveFlight(
        flightEntity: FlightEntity,
        flightOfficesEntity: List<FlightOfficeEntity>,
    ): Completable {
        return flightDao.insertFlight(flightEntity)
            .andThen(flightDao.insertFlightOffice(flightOfficesEntity))
    }

    override fun readFlight(): Flowable<SuccessOrEmptyData<FlightData>> {
        return flightDao.readFlight().map { convertFlight(it) }
    }

    private fun convertFlight(flightData: FlightDataEntity?): SuccessOrEmptyData<FlightData> {
        return if (flightData == null) {
            SuccessOrEmptyData.Empty()
        } else {
            successData(flightData)
        }
    }

    private fun successData(flightDataEntity: FlightDataEntity): SuccessOrEmptyData<FlightData> {
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

    }

    override fun saveBoxesFromFlight(boxesEntity: List<FlightBoxEntity>): Completable {
        return flightDao.insertFlightBoxes(boxesEntity)
    }

    override fun readBoxesFromFlight(): Single<List<FlightBoxEntity>> {
        return Single.error(Throwable())
    }

    override fun removeBoxesFromFlight() {

    }

}