package com.wb.logistics.ui.unloadingreturnboxes.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingDstOfficeEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingSrcOfficeEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.utils.managers.TimeManager
import io.reactivex.Completable
import io.reactivex.Observable

class UnloadingReturnInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
) : UnloadingReturnInteractor {

    override fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable {
        return flight().flatMapCompletable { removeBoxes(it, currentOfficeId, checkedBoxes) }
            .andThen(appLocalRepository.findReturnFlightBoxes(checkedBoxes))
            .flatMap { flightBoxes ->
                Observable.fromIterable(flightBoxes)
                    .map { convertToPvzMatchingBox(it) }
                    .toList()
                    .flatMapCompletable { appLocalRepository.savePvzMatchingBoxes(it) }
                    .toSingle { flightBoxes }
            }
            .flatMapCompletable { appLocalRepository.deleteFlightBoxes(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun removeBoxes(
        flightEntity: FlightEntity,
        dstOfficeId: Int,
        checkedBoxes: List<String>,
    ) = appRemoteRepository.removeBoxesFromFlight(flightEntity.id.toString(),
        false,
        timeManager.getOffsetLocalTime(),
        dstOfficeId,
        checkedBoxes)

    private fun convertToPvzMatchingBox(flightBoxEntity: FlightBoxEntity): PvzMatchingBoxEntity {
        return with(flightBoxEntity) {
            PvzMatchingBoxEntity(barcode = barcode,
                srcOffice = PvzMatchingSrcOfficeEntity(id = srcOffice.id,
                    name = srcOffice.name,
                    fullAddress = srcOffice.fullAddress,
                    longitude = srcOffice.longitude,
                    latitude = srcOffice.latitude),
                dstOffice = PvzMatchingDstOfficeEntity(id = dstOffice.id,
                    name = dstOffice.name,
                    fullAddress = dstOffice.fullAddress,
                    longitude = dstOffice.longitude,
                    latitude = dstOffice.latitude))
        }
    }

    override fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}
