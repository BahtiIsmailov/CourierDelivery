package ru.wb.go.ui.unloadingboxes.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.network.rx.RxSchedulerFactory
import io.reactivex.Observable

class UnloadingBoxesInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : UnloadingBoxesInteractor {

    override fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeUnloadedFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}
