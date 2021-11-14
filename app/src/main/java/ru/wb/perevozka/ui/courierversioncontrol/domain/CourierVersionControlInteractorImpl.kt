package ru.wb.perevozka.ui.courierversioncontrol.domain

import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory

class CourierVersionControlInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierVersionControlInteractor
