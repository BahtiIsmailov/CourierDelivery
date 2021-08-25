package ru.wb.perevozka.ui.couriercarnumber.domain

import io.reactivex.Completable

interface CourierCarNumberInteractor {

    fun putCarNumbers(carNumber: String): Completable

}