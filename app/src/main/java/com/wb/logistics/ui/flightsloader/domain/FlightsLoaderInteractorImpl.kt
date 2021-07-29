package com.wb.logistics.ui.flightsloader.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FlightsLoaderInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : FlightsLoaderInteractor {

    override fun sessionInfo(): Single<UserInfoEntity> {
        return authRemoteRepository.userInfo().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun updateFlight(): Single<FlightDefinitionAction> {
        clearDB()
        return appRemoteRepository.flight()
            .flatMap {
                if (it.flight.id == 0) {
                    flightEmpty()
                } else {
                    appLocalRepository.saveFlightAndOffices(it.flight, it.offices)
                        .andThen(navigateTo())
                }
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flightEmpty() = Single.just(FlightDefinitionAction.FlightEmpty)

    private fun clearDB() {
        appLocalRepository.deleteAllFlight()
        appLocalRepository.deleteFlightOffices()
        appLocalRepository.deleteAllFlightBoxes()
        appLocalRepository.deleteAllWarehouseMatchingBox()
        appLocalRepository.deleteAllPvzMatchingBox()
    }

    private fun navigateTo(): Single<FlightDefinitionAction> {
        return updateTime()
            .andThen(appLocalRepository.readFlightId())
            .flatMap { flightId ->
                Completable.mergeArray(
                    saveWarehouseMatchingBoxes(flightId),
                    savePvzMatchingBoxes(flightId),
                    saveFlightBoxes(flightId))
                    .toSingle { flightId }
            }
            .flatMap { navDirection(it) }
            .map { FlightDefinitionAction.NavigateComplete(it) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun navDirection(flightId: String) = screenManager.navDirection(flightId)

    private fun saveWarehouseMatchingBoxes(flightId: String): Completable {
        return appRemoteRepository.warehouseMatchingBoxes(flightId)
            .flatMapCompletable { appLocalRepository.saveWarehouseMatchingBoxes(it) }
    }

    private fun savePvzMatchingBoxes(flightId: String): Completable {
        return Completable.fromSingle(appLocalRepository.readPvzMatchingBoxes()
            .map { it.isNotEmpty() }
            .filter { it }
            .switchIfEmpty(reloadPvzAttachedBoxes(flightId)))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun reloadPvzAttachedBoxes(flightId: String) =
        appRemoteRepository.pvzMatchingBoxes(flightId)
            .flatMapCompletable { appLocalRepository.savePvzMatchingBoxes(it) }
            .toSingle { true }


    private fun saveFlightBoxes(flightId: String): Completable {
        return appRemoteRepository.flightBoxes(flightId).flatMapCompletable { saveFlightBoxes(it) }
    }

    private fun saveFlightBoxes(flightBoxesEntity: List<FlightBoxEntity>): Completable {
        appLocalRepository.deleteAllFlightBoxes()
        return appLocalRepository.saveFlightBoxes(flightBoxesEntity)
    }

    private fun updateTime() = appRemoteRepository.time()
        .flatMapCompletable { Completable.fromAction { timeManager.saveNetworkTime(it.currentTime) } }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }


}