package ru.wb.perevozka.ui.couriercarnumber.domain

import io.reactivex.Completable
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CarNumberEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory

class CourierCarNumberInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRemoteRepository,
) : CourierCarNumberInteractor {

    override fun putCarNumbers(carNumber: String): Completable {
        return appRepository.putCarNumbers(listOf(CarNumberEntity(carNumber, false)))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}