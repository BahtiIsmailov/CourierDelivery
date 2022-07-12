package ru.wb.go.ui.couriercompletedelivery.domain

import ru.wb.go.db.CourierLocalRepository

class CourierCompleteDeliveryInteractorImpl(
    private val courierLocalRepository: CourierLocalRepository,
) : CourierCompleteDeliveryInteractor
