package com.wb.logistics.ui.unloadingcongratulation.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class CongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
) : CongratulationInteractor {

    override fun groupAttachedBox(): Single<AttachedBoxResultEntity> {
        return appRepository.groupAttachedBox().compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
