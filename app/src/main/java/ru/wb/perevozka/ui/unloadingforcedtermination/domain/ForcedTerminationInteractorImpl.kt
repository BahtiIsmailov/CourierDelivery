package ru.wb.perevozka.ui.unloadingforcedtermination.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager
import io.reactivex.Completable
import io.reactivex.Observable

class ForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : ForcedTerminationInteractor {

    override fun observeNotUnloadedBoxBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeTakeOnFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeUnloading(currentOfficeId: Int, dataLog: String): Completable {
        val date = timeManager.getOffsetLocalTime()
        return insertNotUnloadingBoxToDeliveryError(currentOfficeId)
            .andThen(switchScreenUnloading(currentOfficeId))
            .andThen(switchScreenInTransit(currentOfficeId))
            .andThen(getFlightId())
            .flatMapCompletable { flightsLogs(it, date, dataLog) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun insertNotUnloadingBoxToDeliveryError(currentOfficeId: Int) =
        appLocalRepository.insertNotUnloadingBoxToDeliveryErrorByOfficeId(currentOfficeId)

    private fun flightsLogs(it: Int, date: String, dataLog: String) =
        appRemoteRepository.flightsLogs(it, date, dataLog)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun getFlightId() = appLocalRepository.readFlight().map { it.id }

    private fun switchScreenInTransit(currentOfficeId: Int): Completable {
        return screenManager.saveState(FlightStatus.INTRANSIT, currentOfficeId)
    }

    private fun switchScreenUnloading(currentOfficeId: Int): Completable {
        return screenManager.saveState(FlightStatus.UNLOADING, currentOfficeId)
    }

}
