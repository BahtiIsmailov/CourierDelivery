package ru.wb.perevozka.ui.couriercompletedelivery.domain

import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Single
import ru.wb.perevozka.db.CourierLocalRepository

class CourierCompleteDeliveryInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierCompleteDeliveryInteractor {

//    override fun getCompleteDeliveryResult(): Single<CompleteDeliveryResult> {
//        return courierLocalRepository.completeDeliveryResult()
//            .compose(rxSchedulerFactory.applySingleSchedulers())
//    }

}

//data class CompleteDeliveryResult(val amount: Int, val unloadedCount: Int, val fromCount: Int)
