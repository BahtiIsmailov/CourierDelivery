package ru.wb.go.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory

class CourierBillingAccountDataInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
) : CourierBillingAccountDataInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun saveBillingAccounts(accountsEntity: List<CourierBillingAccountEntity>): Completable {
        return Single.just(accountsEntity)
            .map { it.convertToAccountEntity() }
            .flatMapCompletable { appRemoteRepository.setBankAccounts(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun getBillingAccounts(): Single<List<CourierBillingAccountEntity>> {
        return appRemoteRepository.getBankAccounts()
            .map { it.converToCourierBillingAccountsEntity() }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun BankAccountsEntity.converToCourierBillingAccountsEntity(): List<CourierBillingAccountEntity> {
        val bae = mutableListOf<CourierBillingAccountEntity>()
        data.forEach {
            bae.add(
                CourierBillingAccountEntity(
                    userName = it.name,
                    inn = inn,
                    account = it.account,
                    correspondentAccount = it.correspondentAccount,
                    bic = it.bic,
                    bank = it.name
                )
            )
        }
        return bae
    }

    private fun List<CourierBillingAccountEntity>.convertToAccountEntity(): List<AccountEntity> {
        val accountEntity = mutableListOf<AccountEntity>()
        forEach {
            accountEntity.add(
                AccountEntity(
                    bic = it.bic,
                    name = it.bank,
                    correspondentAccount = it.correspondentAccount,
                    account = it.account
                )
            )
        }
        return accountEntity
    }

    override fun getBank(bic: String): Maybe<BankEntity> {
        return appRemoteRepository.getBank(bic)
            .compose(rxSchedulerFactory.applyMaybeSchedulers())
    }

}