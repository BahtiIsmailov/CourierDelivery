package com.wb.logistics.ui.forcedtermination.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class ForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : ForcedTerminationInteractor {

    override fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeUnloading(dstOfficeId: Int, cause: String): Completable {
        return appLocalRepository.changeFlightOfficeUnloading(dstOfficeId, true, cause)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}
