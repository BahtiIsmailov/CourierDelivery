package ru.wb.perevozka.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.perevozka.network.api.app.entity.PaymentEntity
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory

class CourierBillingAccountSelectorInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierBillingAccountSelectorInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun courierDocuments(courierDocumentsEntity: CourierBillingAccountEntity): Completable {
        return Completable.complete()
//        return appRemoteRepository.courierDocuments(courierDocumentsEntity)
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun accounts(): Single<List<CourierBillingAccountEntity>> {
        return courierLocalRepository.readAllAccounts()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun payments(paymentEntity: PaymentEntity): Completable {
        return appRemoteRepository.payments(paymentEntity)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}