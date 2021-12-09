package ru.wb.go.ui.dcunloadingforcedtermination.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.FlightStatus
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.ScreenManager
import ru.wb.go.utils.managers.TimeManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DcForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
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

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun getFlightId() = appLocalRepository.readFlight().map { it.id }

}
