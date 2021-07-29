package com.wb.logistics.ui.flightpickpoint.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.deliveryboxes.PickupPointBoxGroupByOfficeEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.utils.managers.ScreenManager
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