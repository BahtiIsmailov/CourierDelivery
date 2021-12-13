package ru.wb.go.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory

class CourierBillingAccountDataInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
) : CourierBillingAccountDataInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun saveAccount(courierBillingAccountEntity: CourierBillingAccountEntity): Completable {
        return courierLocalRepository.saveAccount(courierBillingAccountEntity)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun getAccount(account: String): Single<CourierBillingAccountEntity> {
        return courierLocalRepository.readAccount(account)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getBank(bic: String): Maybe<BankEntity> {
        return appRemoteRepository.getBank(bic)
            .compose(rxSchedulerFactory.applyMaybeSchedulers())
    }

}