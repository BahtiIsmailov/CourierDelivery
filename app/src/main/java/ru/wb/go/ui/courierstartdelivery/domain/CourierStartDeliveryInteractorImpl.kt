package ru.wb.go.ui.courierstartdelivery.domain

import ru.wb.go.db.CourierLocalRepository

class CourierStartDeliveryInteractorImpl(
    private val courierLocalRepository: CourierLocalRepository,
) : CourierStartDeliveryInteractor
