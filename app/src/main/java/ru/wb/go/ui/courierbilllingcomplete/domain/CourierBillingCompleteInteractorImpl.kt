package ru.wb.go.ui.courierbilllingcomplete.domain

import ru.wb.go.db.CourierLocalRepository

class CourierBillingCompleteInteractorImpl(
    private val courierLocalRepository: CourierLocalRepository,
) : CourierBillingCompleteInteractor
