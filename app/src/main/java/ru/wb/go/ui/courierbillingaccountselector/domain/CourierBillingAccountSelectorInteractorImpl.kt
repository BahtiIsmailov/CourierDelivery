package ru.wb.go.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingAccountSelectorInteractorImpl(
        private val rxSchedulerFactory: RxSchedulerFactory,
        private val networkMonitorRepository: NetworkMonitorRepository,
        private val appRemoteRepository: AppRemoteRepository,
        private val courierLocalRepository: CourierLocalRepository,
        private val deviceManager: DeviceManager,
        private val userManager: UserManager
) : CourierBillingAccountSelectorInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
                .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun accounts(): Single<List<CourierBillingAccountEntity>> {
        return courierLocalRepository.readAllAccounts()
                .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun payments(amount: Int, paymentEntity: PaymentEntity): Completable {
        return initGuid()
                .flatMapCompletable { guid -> appRemoteRepository.payments(guid, amount, paymentEntity) }
                .doOnComplete { userManager.clearPaymentGuid() }
                .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun initGuid(): Single<String> {
        return Single.fromCallable {
            if (userManager.getPaymentGuid().isEmpty()) {userManager.savePaymentGuid(deviceManager.guid())}
            userManager.getPaymentGuid()
        }
    }


}