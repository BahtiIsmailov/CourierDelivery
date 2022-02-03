package ru.wb.go.ui.courierbilllingcomplete.domain

import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.network.rx.RxSchedulerFactory

class CourierBillingCompleteInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierBillingCompleteInteractor
