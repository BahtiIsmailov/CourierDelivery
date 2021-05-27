package com.wb.logistics.ui.flightdeliveriesdetails.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class FlightDeliveriesDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appLocalRepository: AppLocalRepository
) : FlightDeliveriesDetailsInteractor {

    override fun getUnloadedAndReturnBoxesGroupByOffice(dstOfficeId: Int): Single<UnloadedAndReturnBoxesGroupByOffice> {
        return Single.zip(
            appLocalRepository.observeUnloadedBoxesByDstOfficeId(dstOfficeId).firstOrError(),
            appLocalRepository.groupByDstAddressReturnBox(dstOfficeId),
            { unloadedBoxes, returnBoxes ->
                UnloadedAndReturnBoxesGroupByOffice(unloadedBoxes, returnBoxes)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

}