package ru.wb.perevozka.ui.unloadingboxes.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
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
