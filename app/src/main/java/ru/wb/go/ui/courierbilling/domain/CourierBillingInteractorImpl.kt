package ru.wb.go.ui.courierbilling.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

class CourierBillingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val tokenManager: TokenManager
) : CourierBillingInteractor {

    override fun billing(): Single<BillingCommonEntity> {
        return appRemoteRepository.billing(true)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
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

    override fun updateAccountsIsExist(): Single<Boolean> {
        return appRemoteRepository.getBankAccounts()
            .flatMap {
                val userName = tokenManager.userName()
                val inn = it.inn
                val billingAccounts = it.data.convertToBillingAccounts(userName, inn)
                tokenManager.userInn(inn)
                courierLocalRepository.deleteAllAccount()
                    .andThen(courierLocalRepository.saveAccounts(billingAccounts))
                    .andThen(Single.just(billingAccounts.isNotEmpty()))
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}