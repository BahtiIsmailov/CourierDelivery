package com.wb.logistics.ui.forcedtermination.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class ForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
) : ForcedTerminationInteractor {

    override fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appRepository.observedAttachedBoxes(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeUnloading(dstOfficeId: Int, cause: String): Completable {
        return appRepository.changeFlightOfficeUnloading(dstOfficeId, true, cause)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}
