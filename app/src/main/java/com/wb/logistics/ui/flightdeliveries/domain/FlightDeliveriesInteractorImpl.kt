package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
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

    override fun getAttachedBoxesGroupByOffice(): Single<List<AttachedBoxGroupByOfficeEntity>> {
        return appLocalRepository.groupAttachedBoxByDstAddress()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getAttachedBoxes(): Single<Int> {
        return appLocalRepository.observeAttachedBoxes()
            .map { it.size }
            .firstOrError()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun switchScreen(): Completable {
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

    override fun flightId(): Single<Int> {
        return appLocalRepository.observeFlightDataOptional()
            .map {
                when (it) {
                    is Optional.Empty -> 0
                    is Optional.Success -> it.data.flightId
                }
            }.firstOrError()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}