package com.wb.logistics.ui.flightdeliveries.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxGroupByOfficeEntity
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class FlightDeliveriesInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appLocalRepository: AppLocalRepository,
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

//    override fun getUnloadedAndReturnBoxesGroupByOffice(dstOfficeId: Int): Single<UnloadedAndReturnBoxesGroupByOffice> {
//        return Single.zip(
//            appRepository.observeUnloadedBoxesByDstOfficeId(dstOfficeId).firstOrError(),
//            appRepository.observedReturnBoxesByDstOfficeId(dstOfficeId).firstOrError(),
//            { unloadedBoxes, returnBoxes ->
//                UnloadedAndReturnBoxesGroupByOffice(unloadedBoxes, returnBoxes)
//            }
//        ).compose(rxSchedulerFactory.applySingleSchedulers())
//    }

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