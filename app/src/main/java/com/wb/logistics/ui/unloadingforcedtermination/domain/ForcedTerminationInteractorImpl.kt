package com.wb.logistics.ui.unloadingforcedtermination.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable

class ForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : ForcedTerminationInteractor {

    override fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeTakeOnFlightBoxesByOfficeId(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeUnloading(currentOfficeId: Int, data: String): Completable {
        return switchScreenUnloading(currentOfficeId)
            .andThen(switchScreenInTransit(currentOfficeId))
            .andThen(getFlightId())
            .flatMapCompletable { flightsLogs(it, data) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun flightsLogs(it: Int, data: String) =
        appRemoteRepository.flightsLogs(it, timeManager.getOffsetLocalTime(), data)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun getFlightId() = appLocalRepository.readFlight().map { it.id }

    private fun switchScreenInTransit(currentOfficeId: Int): Completable {
        return screenManager.saveState(FlightStatus.INTRANSIT, currentOfficeId)
    }

    private fun switchScreenUnloading(currentOfficeId: Int): Completable {
        return screenManager.saveState(FlightStatus.UNLOADING, currentOfficeId)
    }

}
