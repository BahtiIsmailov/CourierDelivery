package ru.wb.go.ui.courierversioncontrol.domain

import ru.wb.go.db.CourierLocalRepository

class CourierVersionControlInteractorImpl(
    private val courierLocalRepository: CourierLocalRepository,
) : CourierVersionControlInteractor
