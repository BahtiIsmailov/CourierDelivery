package ru.wb.perevozka.ui.couriercarnumber.domain

import io.reactivex.Completable

interface CourierCarNumberInteractor {

    fun putCarNumber(carNumber: String): Completable

}