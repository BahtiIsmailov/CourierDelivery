package ru.wb.perevozka.ui.dcunloadingforcedtermination.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager
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
