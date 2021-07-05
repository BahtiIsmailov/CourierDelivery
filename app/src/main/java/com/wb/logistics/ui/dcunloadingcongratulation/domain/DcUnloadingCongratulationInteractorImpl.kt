package com.wb.logistics.ui.dcunloadingcongratulation.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.unloadingcongratulation.domain.DeliveryResult
import io.reactivex.Observable
import io.reactivex.Single

class DcUnloadingCongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : DcUnloadingCongratulationInteractor {

    override fun congratulation(): Single<DcCongratulationEntity> {
        return Observable.zip(
            observeDcUnloadedBoxes(),
            observeDeliveryBoxes(),
            { unloaded, delivery ->
                DcCongratulationEntity(
                    delivery.unloadedCount,
                    delivery.attachedCount,
                    unloaded.dcUnloadingCount,
                    unloaded.dcReturnCount
                )
            })
            .firstOrError()
            .doOnSuccess { appLocalRepository.deleteAll() }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observeDcUnloadedBoxes(): Observable<DcUnloadingScanBoxEntity> {
        return appLocalRepository.readFlight().map { it.dc.id }
            .flatMapObservable { currentOfficeId ->
                appLocalRepository.observeDcUnloadingScanBox(currentOfficeId).toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun observeDeliveryBoxes(): Observable<DeliveryResult> {
        return appLocalRepository.groupDeliveryBoxByOffice()
            .flatMap { boxes ->
                Observable.fromIterable(boxes)
                    .map {
                        DeliveryResult(it.unloadedCount,
                            it.attachedCount)
                    }
                    .reduce(DeliveryResult(0, 0),
                        { accumulator, item ->
                            val attachedCount = accumulator.attachedCount + item.attachedCount
                            val unloadedCount = accumulator.unloadedCount + item.unloadedCount
                            DeliveryResult(unloadedCount, attachedCount)
                        })
            }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}
