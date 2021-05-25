package com.wb.logistics.ui.dcunloadingcongratulation.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class DcUnloadingCongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
) : DcUnloadingCongratulationInteractor {

    override fun congratulation(): Single<DcCongratulationEntity> {
        return appRepository.congratulation().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun groupAttachedBox(): Single<AttachedBoxResultEntity> {
        return appRepository.groupAttachedBox().compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
