package com.wb.logistics.ui.dcunloadingcongratulation.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class DcUnloadingCongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : DcUnloadingCongratulationInteractor {

    override fun congratulation(): Single<DcCongratulationEntity> {
        return appLocalRepository.congratulation()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun groupAttachedBox(): Single<AttachedBoxResultEntity> {
        return appLocalRepository.groupAttachedBox()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
