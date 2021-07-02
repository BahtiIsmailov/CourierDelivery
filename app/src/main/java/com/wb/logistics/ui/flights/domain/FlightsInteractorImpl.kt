package com.wb.logistics.ui.flights.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.Optional
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Flowable

class FlightsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
) : FlightsInteractor {

    override fun observeFlightData(): Flowable<Optional<FlightData>> {
        return appLocalRepository.observeFlightDataOptional()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun observeFlightBoxScanned(): Flowable<Int> {
        return appLocalRepository.observeTakeOnFlightBoxesByOfficeId().map { it.size }
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

}