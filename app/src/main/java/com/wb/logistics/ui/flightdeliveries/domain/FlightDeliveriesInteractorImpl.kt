package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.deliveryboxes.DeliveryBoxGroupByOfficeEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Single

class FlightDeliveriesInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val screenManager: ScreenManager,
) : FlightDeliveriesInteractor {

    override fun flightId(): Single<String> {
        return appLocalRepository.readFlightId().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getDeliveryBoxesGroupByOffice(): Single<List<DeliveryBoxGroupByOfficeEntity>> {
        return appLocalRepository.groupDeliveryBoxByOffice()
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

    private fun reloadPvzAttachedBoxes() = appLocalRepository.readFlightId()
        .flatMap { flightId ->
            appRemoteRepository.pvzMatchingBoxes(flightId)
                .flatMapCompletable { appLocalRepository.savePvzMatchingBoxes(it) }
                .toSingle { true }
        }

}