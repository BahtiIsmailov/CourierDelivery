package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Single

class FlightDeliveriesInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
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
        return  screenManager.saveState(FlightStatus.DCUNLOADING)
    }

    override fun flightId(): Single<Int> {
        return appLocalRepository.observeFlightWrap()
            .map {
                when (it) {
                    is SuccessOrEmptyData.Empty -> 0
                    is SuccessOrEmptyData.Success -> it.data.flightId
                }
            }.firstOrError()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}