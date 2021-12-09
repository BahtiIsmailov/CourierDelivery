package ru.wb.go.ui.courierstartdelivery.domain

import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.db.CourierLocalRepository

class CourierStartDeliveryInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierStartDeliveryInteractor
