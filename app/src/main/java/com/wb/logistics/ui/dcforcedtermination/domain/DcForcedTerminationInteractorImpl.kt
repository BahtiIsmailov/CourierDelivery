package com.wb.logistics.ui.dcforcedtermination.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DcForcedTerminationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : DcForcedTerminationInteractor {

    override fun observeDcUnloadedBoxes(): Observable<DcUnloadingScanBoxEntity> {
        return appLocalRepository.observeDcUnloadingScanBox()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeUnloading(dstOfficeId: Int, cause: String): Completable {
        return appLocalRepository.changeFlightOfficeUnloading(dstOfficeId, true, cause)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>> {
        return appLocalRepository.notDcUnloadedBoxes().compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
