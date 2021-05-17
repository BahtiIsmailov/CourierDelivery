package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class FlightDeliveriesInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRepository: AppRepository,
) : FlightDeliveriesInteractor {

    override fun getAttachedBoxesGroupByOffice(): Single<List<AttachedBoxGroupByOfficeEntity>> {
        return appRepository.groupAttachedBoxesByDstAddress()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getAttachedBoxes(): Single<Int> {
        return appRepository.observeAttachedBoxes()
            .map { it.size }
            .firstOrError()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun flightId(): Single<Int> {
        return appRepository.observeFlight()
            .map {
                when (it) {
                    is SuccessOrEmptyData.Empty -> 0
                    is SuccessOrEmptyData.Success -> it.data.flight
                }
            }.firstOrError()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}