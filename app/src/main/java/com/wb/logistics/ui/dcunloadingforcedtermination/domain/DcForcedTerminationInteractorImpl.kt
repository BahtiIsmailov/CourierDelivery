package com.wb.logistics.ui.dcunloadingforcedtermination.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DcForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : DcForcedTerminationInteractor {

    // TODO: 09.07.2021 переработать
    override fun observeNotDcUnloadedBoxes(): Observable<Int> {
        return appLocalRepository.readFlight().map { it.dc.id }
            .flatMap { appLocalRepository.findDcReturnBoxes(it) }
            .map { it.size }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    // TODO: 09.07.2021 переработать
    override fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>> {
        return appLocalRepository.readFlight().map { it.dc.id }
            .flatMap { dcId -> appLocalRepository.findDcReturnBoxes(dcId).map { Pair(dcId, it) } }
            .flatMap { boxes ->
                Observable.fromIterable(boxes.second).map {
                    DcNotUnloadedBoxEntity(
                        barcode = it.barcode,
                        updatedAt = it.updatedAt,
                        srcFullAddress = it.srcOffice.fullAddress,
                        currentOffice = boxes.first,
                        srcOffice = it.srcOffice.id,
                    )
                }.toList()
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun switchScreenToClosed(data: String): Completable {
        return getFlightId().flatMapCompletable {
            appRemoteRepository.flightsLogs(it,
                timeManager.getOffsetLocalTime(),
                data)
        }
            .andThen(screenManager.saveState(FlightStatus.CLOSED))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun getFlightId() = appLocalRepository.readFlight().map { it.id }

}
