package ru.wb.go.ui.couriercarnumber.domain

import io.reactivex.Completable
import ru.wb.go.network.token.UserManager

class CourierCarNumberInteractorImpl(
    private val userManager: UserManager
) : CourierCarNumberInteractor {

    override fun putCarTypeAndNumber(carType: Int, carNumber: String) {
        userManager.saveCarType(carType)
        userManager.saveCarNumber(carNumber)
    }

    override fun getCarNumber(): String {
       return userManager.carNumber()
    }

    override fun getCarType(): Int {
        return userManager.carType()
    }

    /*
    override fun putCarTypeAndNumber(carType: Int, carNumber: String): Completable {
        userManager.saveCarType(carType)
        userManager.saveCarNumber(carNumber)
        return Completable.complete()
    }

    override fun getCarNumber(): String {
       return userManager.carNumber()
    }

    override fun getCarType(): Int {
        return userManager.carType()
    }

     */
}