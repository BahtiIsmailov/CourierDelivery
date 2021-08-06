package ru.wb.perevozka.ui.flights.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.FlightData
import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
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