package ru.wb.perevozka.ui.courierstartdelivery.domain

import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.db.CourierLocalRepository

class CourierStartDeliveryInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierStartDeliveryInteractor
