package ru.wb.perevozka.ui.courierbilling.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.BillingCommonEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.LogUtils

class CourierBillingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,

    ) : CourierBillingInteractor {

    override fun billing(): Single<BillingCommonEntity> {
        return appRemoteRepository.billing(true)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun accounts(): Completable {
        return courierLocalRepository.readAllAccounts()
            .doOnSuccess { LogUtils { logDebugApp(it.toString()) } }
            .flatMapCompletable { if (it.isEmpty()) Completable.error(Throwable()) else Completable.complete() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}