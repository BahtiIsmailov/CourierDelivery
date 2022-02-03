package ru.wb.go.ui.courierexpects.domain

import io.reactivex.Single

interface CourierExpectsInteractor {

    fun isRegisteredStatus(): Single<String>

}