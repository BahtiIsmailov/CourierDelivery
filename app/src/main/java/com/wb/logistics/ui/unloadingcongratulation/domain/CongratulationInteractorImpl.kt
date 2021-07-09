package com.wb.logistics.ui.unloadingcongratulation.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class CongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : CongratulationInteractor {

    override fun getDeliveryBoxesGroupByOffice(): Single<DeliveryResult> {
        return appLocalRepository.getCongratulationDelivered()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}

data class DeliveryResult(val unloadedCount: Int, val attachedCount: Int)
