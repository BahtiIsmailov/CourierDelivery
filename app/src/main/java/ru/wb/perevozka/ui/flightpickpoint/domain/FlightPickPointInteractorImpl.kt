package ru.wb.perevozka.ui.flightpickpoint.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FlightPickPointInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val screenManager: ScreenManager,
) : FlightPickPointInteractor {

    override fun flightId(): Single<Int> {
        return appLocalRepository.readFlight().map { it.id }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getAttachedBoxesGroupByOffice(): Single<List<PickupPointBoxGroupByOfficeEntity>> {
        return appLocalRepository.groupFlightPickupPointBoxGroupByOffice()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun createTTN(): Completable {
        return Completable.mergeArray(switchScreenToDelivery(), updatePvzMatchingBoxes())
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun switchScreenToDelivery() = screenManager.saveState(FlightStatus.INTRANSIT)

    private fun updatePvzMatchingBoxes(): Completable {
        return appLocalRepository.readFlightId()
            .flatMapCompletable { flightId ->
                appRemoteRepository.pvzMatchingBoxes(flightId)
                    .flatMapCompletable { appLocalRepository.savePvzMatchingBoxes(it) }
            }
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}