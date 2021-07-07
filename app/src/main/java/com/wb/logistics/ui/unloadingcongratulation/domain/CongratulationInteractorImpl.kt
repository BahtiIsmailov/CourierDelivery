package com.wb.logistics.ui.unloadingcongratulation.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import io.reactivex.Single

class CongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : CongratulationInteractor {

    override fun getDeliveryBoxesGroupByOffice(): Single<DeliveryResult> {
        return appLocalRepository.groupDeliveryBoxByOffice()
            .flatMap { boxes ->
                Observable.fromIterable(boxes)
                    .map {
                        DeliveryResult(it.deliveredCount,
                            it.deliverCount)
                    }
                    .reduce(DeliveryResult(0, 0),
                        { accumulator, item ->
                            val attachedCount = accumulator.attachedCount + item.attachedCount
                            val unloadedCount = accumulator.unloadedCount + item.unloadedCount
                            DeliveryResult(unloadedCount, attachedCount)
                        })
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}

data class DeliveryResult(val unloadedCount: Int, val attachedCount: Int)
