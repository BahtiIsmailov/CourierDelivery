package com.wb.logistics.ui.unloadingcongratulation.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class CongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : CongratulationInteractor {

    override fun groupAttachedBox(): Single<AttachedBoxResultEntity> {
        return appLocalRepository.groupAttachedBox().compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
