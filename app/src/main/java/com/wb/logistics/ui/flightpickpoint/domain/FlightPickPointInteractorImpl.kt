package com.wb.logistics.ui.flightpickpoint.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class FlightPickPointInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appLocalRepository: AppLocalRepository,
) : FlightPickPointInteractor {

    override fun getAttachedBoxesGroupByOffice(): Single<List<AttachedBoxGroupByOfficeEntity>> {
        return appLocalRepository.groupAttachedBoxByDstAddress()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun flightId(): Single<Int> {
        return appLocalRepository.observeFlight()
            .map {
                when (it) {
                    is SuccessOrEmptyData.Empty -> 0
                    is SuccessOrEmptyData.Success -> it.data.flight
                }
            }.firstOrError()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}