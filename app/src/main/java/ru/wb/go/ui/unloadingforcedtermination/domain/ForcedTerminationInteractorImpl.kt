package ru.wb.go.ui.unloadingforcedtermination.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.FlightStatus
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.ScreenManager
import ru.wb.go.utils.managers.TimeManager
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
