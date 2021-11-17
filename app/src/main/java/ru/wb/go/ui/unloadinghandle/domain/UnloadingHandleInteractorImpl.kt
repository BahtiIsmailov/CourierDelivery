package ru.wb.go.ui.unloadinghandle.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.network.rx.RxSchedulerFactory
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
