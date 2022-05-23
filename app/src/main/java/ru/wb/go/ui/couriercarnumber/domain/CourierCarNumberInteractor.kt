package ru.wb.go.ui.couriercarnumber.domain

import io.reactivex.Completable

interface CourierCarNumberInteractor {

    fun putCarTypeAndNumber(carType: Int, carNumber: String): Completable

    fun getCarNumber(): String

    fun getCarType(): Int

}