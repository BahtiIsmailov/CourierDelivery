package ru.wb.go.ui.couriercompletedelivery.domain

import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.db.CourierLocalRepository

class CourierCompleteDeliveryInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierCompleteDeliveryInteractor
