package ru.wb.go.ui.couriercarnumber.domain

import io.reactivex.Completable
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager

class CourierCarNumberInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRemoteRepository,
    private val userManager: UserManager
) : CourierCarNumberInteractor {

    override fun putCarNumber(carNumber: String): Completable {
        userManager.saveCarNumber(carNumber)
        return Completable.complete()
    }

}