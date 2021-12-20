package ru.wb.go.ui.courierversioncontrol.domain

import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.network.rx.RxSchedulerFactory

class CourierVersionControlInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierVersionControlInteractor
