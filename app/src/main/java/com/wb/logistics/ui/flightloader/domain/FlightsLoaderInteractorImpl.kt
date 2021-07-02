package com.wb.logistics.ui.flightloader.domain

import androidx.navigation.NavDirections
import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedDstOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedSrcOfficeEntity
import com.wb.logistics.db.entity.flighboxes.BoxStatus
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FlightsLoaderInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : FlightsLoaderInteractor {

    override fun sessionInfo(): Single<UserInfoEntity> {
        return authRemoteRepository.userInfo().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun navigateTo(): Single<NavDirections> {
        appLocalRepository.deleteAll()
        return updateFlightAndTime()
            .andThen(appLocalRepository.readFlightId())
            .flatMap { flightId ->
                Completable.mergeArray(
                    saveWarehouseMatchingBoxes(flightId),
                    saveFlightBoxes(flightId))
                    .toSingle { flightId }
            }
            .flatMap { navDirection(it) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun navDirection(flightId: String) = screenManager.navDirection(flightId)

    private fun saveWarehouseMatchingBoxes(flightId: String): Completable {
        return appRemoteRepository.warehouseMatchingBoxes(flightId)
            .flatMapCompletable { appLocalRepository.saveWarehouseMatchingBoxes(it) }
    }

    private fun saveFlightBoxes(flightId: String): Completable {
        return appRemoteRepository.flightBoxes(flightId)
            .flatMapCompletable {
                Completable.mergeArray(saveFlightBoxes(it), deleteAndUpdateAttachedBoxes(it))
            }
    }

    private fun saveFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>) =
        appLocalRepository.saveFlightBoxes(flightBoxesEntity)

    private fun deleteAndUpdateAttachedBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable {
        appLocalRepository.deleteAllAttachedBox()
        return Observable.fromIterable(flightBoxesEntity)
            .filter { it.status == BoxStatus.TAKE_ON_FLIGHT.ordinal }
            .map { convertToAttachedBox(it) }
            .toList()
            .flatMapCompletable { appLocalRepository.saveAttachedBoxes(it) }
    }

    private fun convertToAttachedBox(flightBoxEntity: FlightBoxEntity) =
        with(flightBoxEntity) {
            AttachedBoxEntity(
                barcode = flightBoxEntity.barcode,
                srcOffice = AttachedSrcOfficeEntity(
                    id = srcOffice.id,
                    name = srcOffice.name,
                    fullAddress = srcOffice.fullAddress,
                    longitude = srcOffice.longitude,
                    latitude = srcOffice.latitude,
                ),
                dstOffice = AttachedDstOfficeEntity(
                    id = dstOffice.id,
                    name = dstOffice.name,
                    fullAddress = dstOffice.fullAddress,
                    longitude = dstOffice.longitude,
                    latitude = dstOffice.latitude,
                ),
                isManualInput = false,
                updatedAt = updatedAt)
        }

    private fun updateFlightAndTime() = Completable.mergeArray(flight(), time())

    private fun time() = appRemoteRepository.time()
        .flatMapCompletable { Completable.fromAction { timeManager.saveNetworkTime(it.currentTime) } }

    private fun flight() = appRemoteRepository.flight()
        .flatMapCompletable { appLocalRepository.saveFlightAndOffices(it.flight, it.offices) }

}