package com.wb.logistics.ui.flightdeliveriesdetails.domain

import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class FlightDeliveriesDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRepository: AppRepository,
) : FlightDeliveriesDetailsInteractor {

    override fun getUnloadedAndReturnBoxesGroupByOffice(dstOfficeId: Int): Single<UnloadedAndReturnBoxesGroupByOffice> {
        return Single.zip(
            appRepository.observeUnloadedBoxesByDstOfficeId(dstOfficeId).firstOrError(),
            appRepository.groupByDstAddressReturnBox(dstOfficeId),
            { unloadedBoxes, returnBoxes ->
                UnloadedAndReturnBoxesGroupByOffice(unloadedBoxes, returnBoxes)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

}