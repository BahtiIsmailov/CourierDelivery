package com.wb.logistics.ui.dcunloadingcongratulation.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class DcUnloadingCongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : DcUnloadingCongratulationInteractor {

    override fun congratulation(): Single<DcCongratulationEntity> {
        return appLocalRepository.readFlight().map { it.dc.id }
            .flatMap { appLocalRepository.dcUnloadingCongratulation(it) }
            .doOnSuccess { appLocalRepository.deleteAll() }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
