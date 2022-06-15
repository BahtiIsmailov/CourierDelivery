package ru.wb.go.ui.couriercarnumber.domain

interface CourierCarNumberInteractor {

    fun putCarTypeAndNumber(carType: Int, carNumber: String)

    fun getCarNumber(): String

    fun getCarType(): Int

}