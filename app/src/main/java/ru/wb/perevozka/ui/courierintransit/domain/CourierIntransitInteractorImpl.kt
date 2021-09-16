package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Flowable
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory

class CourierIntransitInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierIntransitInteractor {

    override fun observeBoxesGroupByOrder(): Flowable<List<CourierIntransitGroupByOfficeEntity>> {
        return courierLocalRepository.observeBoxesGroupByOffice()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

}