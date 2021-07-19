package com.wb.logistics.ui.unloadingboxes.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
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
