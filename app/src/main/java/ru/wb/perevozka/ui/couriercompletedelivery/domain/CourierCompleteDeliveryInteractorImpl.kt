package ru.wb.perevozka.ui.couriercompletedelivery.domain

import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Single
import ru.wb.perevozka.db.CourierLocalRepository

class CourierCompleteDeliveryInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierCompleteDeliveryInteractor
