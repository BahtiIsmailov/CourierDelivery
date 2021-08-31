package ru.wb.perevozka.ui.couriercarnumber.domain

import io.reactivex.Completable
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CarNumberEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.UserManager

class CourierCarNumberInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRemoteRepository,
    private val userManager: UserManager
) : CourierCarNumberInteractor {

    override fun putCarNumber(carNumber: String): Completable {
        userManager.saveCarNumber(carNumber)
        return appRepository.putCarNumbers(listOf(CarNumberEntity(carNumber, false)))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}