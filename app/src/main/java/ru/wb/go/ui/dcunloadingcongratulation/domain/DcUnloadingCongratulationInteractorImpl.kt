package ru.wb.go.ui.dcunloadingcongratulation.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.db.entity.dcunloadedboxes.DcCongratulationEntity
import ru.wb.go.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.unloadingcongratulation.domain.DeliveryResult
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
            .doOnSuccess { appLocalRepository.clearAll() }
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
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}
