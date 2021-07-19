package com.wb.logistics.ui.unloadinghandle.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
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
