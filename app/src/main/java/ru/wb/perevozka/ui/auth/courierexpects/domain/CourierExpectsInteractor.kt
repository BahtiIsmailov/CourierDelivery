package ru.wb.perevozka.ui.auth.courierexpects.domain

import io.reactivex.Single

interface CourierExpectsInteractor {

    fun isRegisteredStatus(): Single<Boolean>

}