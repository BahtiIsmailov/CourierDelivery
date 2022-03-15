package ru.wb.go.ui.courierdataexpects.domain

import io.reactivex.Single

interface CourierDataExpectsInteractor {

    fun isRegisteredStatus(): Single<String>

}