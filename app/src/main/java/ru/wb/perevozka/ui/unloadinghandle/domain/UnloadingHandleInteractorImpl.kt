package ru.wb.perevozka.ui.unloadinghandle.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Observable

class UnloadingHandleInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : UnloadingHandleInteractor {

    override fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeTakeOnFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}
