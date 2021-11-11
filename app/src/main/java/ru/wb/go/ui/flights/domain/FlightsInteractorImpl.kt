package ru.wb.go.ui.flights.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.FlightData
import ru.wb.go.db.Optional
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
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