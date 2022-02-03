package ru.wb.go.ui.courierbillingaccountselector.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingAccountSelectorInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val deviceManager: DeviceManager,
    private val userManager: UserManager,
    private val tokenManager: TokenManager
) : CourierBillingAccountSelectorInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun payments(amount: Int, paymentEntity: PaymentEntity): Completable {
        return initGuid()
            .flatMapCompletable { guid ->
                appRemoteRepository.payments(
                    guid,
                    amount,
                    paymentEntity
                )
            }
            .doOnComplete { userManager.clearPaymentGuid() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun initGuid(): Single<String> {
        return Single.fromCallable {
            if (userManager.getPaymentGuid().isEmpty()) {
                userManager.savePaymentGuid(deviceManager.guid())
            }
            userManager.getPaymentGuid()
        }
    }

    override fun getBillingAccounts(): Single<List<CourierBillingAccountEntity>> {
        return appRemoteRepository.getBankAccounts()
            .flatMap {
                val userName = tokenManager.userName()
                val inn = it.inn
                val billingAccounts = it.data.convertToBillingAccounts(userName, inn)
                Single.just(billingAccounts)
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun List<AccountEntity>.convertToBillingAccounts(
        userName: String,
        inn: String
    ): List<CourierBillingAccountEntity> {
        val accountsEntity = mutableListOf<CourierBillingAccountEntity>()
        forEach {
            val item = CourierBillingAccountEntity(
                userName = userName,
                inn = inn,
                correspondentAccount = it.correspondentAccount,
                bic = it.bic,
                bank = it.name,
                account = it.account,
            )
            accountsEntity.add(item)
        }
        return accountsEntity
    }
}