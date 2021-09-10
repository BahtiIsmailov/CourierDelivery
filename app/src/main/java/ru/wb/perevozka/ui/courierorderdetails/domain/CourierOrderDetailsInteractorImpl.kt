package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Flowable
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.UserManager

class CourierOrderDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
    private val userManager: UserManager
) : CourierOrderDetailsInteractor {

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun carNumberIsConfirm(): Boolean {
        return userManager.carNumber().isNotEmpty()
    }

}