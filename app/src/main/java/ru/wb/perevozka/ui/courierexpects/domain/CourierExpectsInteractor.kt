package ru.wb.perevozka.ui.courierexpects.domain

import io.reactivex.Single

interface CourierExpectsInteractor {

    fun isRegisteredStatus(): Single<Boolean>

}