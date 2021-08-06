package ru.wb.perevozka.ui.unloadingreturnboxes.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.flight.FlightEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingDstOfficeEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingSrcOfficeEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.TimeManager
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
