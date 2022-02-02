package ru.wb.go.ui.couriercarnumber.domain

import io.reactivex.Completable

interface CourierCarNumberInteractor {

    fun putCarNumber(carNumber: String): Completable

    fun getCarNumber(): String

}