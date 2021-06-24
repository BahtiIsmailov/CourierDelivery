package com.wb.logistics.ui.forcedtermination.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable

class ForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
    private val screenManager: ScreenManager,
) : ForcedTerminationInteractor {

    override fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeUnloading(dstOfficeId: Int, cause: String): Completable {
        return screenManager.saveState(FlightStatus.INTRANSIT)
            //.andThen(appLocalRepository.changeFlightOfficeUnloading(dstOfficeId, true, cause))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}
