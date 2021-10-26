package ru.wb.perevozka.ui.courierbilling.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.BillingCommonEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import java.util.concurrent.TimeUnit

class CourierBillingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
) : CourierBillingInteractor {

    override fun billing(): Single<BillingCommonEntity> {
        return appRemoteRepository.billing(true)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}