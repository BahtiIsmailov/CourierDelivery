package ru.wb.perevozka.ui.flightdeliveries.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FlightDeliveriesInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val screenManager: ScreenManager,
) : FlightDeliveriesInteractor {

    override fun updateFlight() = appRemoteRepository.flight()
        .flatMapCompletable { appLocalRepository.saveFlightAndOffices(it.flight, it.offices) }
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    override fun flightId(): Single<String> {
        return appLocalRepository.readFlightId()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getDeliveryBoxesGroupByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>> {
        return appLocalRepository.groupDeliveryBoxByOffice()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getNotDelivered(): Single<Int> {
        return appLocalRepository.getNotDelivered()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun switchScreenToDcUnloading(): Completable {
        return screenManager.saveState(FlightStatus.DCUNLOADING)
    }

    override fun updatePvzAttachedBoxes(): Completable {
        return Completable.fromSingle(appLocalRepository.readPvzMatchingBoxes()
            .map { it.isNotEmpty() }
            .filter { it }
            .switchIfEmpty(reloadPvzAttachedBoxes()))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun reloadPvzAttachedBoxes() = appLocalRepository.readFlightId()
        .flatMap { flightId ->
            appRemoteRepository.pvzMatchingBoxes(flightId)
                .flatMapCompletable { appLocalRepository.savePvzMatchingBoxes(it) }
                .toSingle { true }
        }

}