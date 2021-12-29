package ru.wb.go.ui.courierbillingaccountdata.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
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

    override fun saveAccountRemote(
            accountEntity: CourierBillingAccountEntity,
            oldAccount: String
    ): Completable {
        // TODO: 13.12.2021 удалить после тестирования
//        return Completable.timer(4, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
        return courierLocalRepository.readAllAccounts()
                .map { it.toMutableList() }
                .map { replaceAccountsEntity(it, accountEntity) }
                .map { it.convertToAccountEntity() }
                .flatMapCompletable { appRemoteRepository.setBankAccounts(it) }
                .andThen(courierLocalRepository.deleteAccount(oldAccount))
                .andThen(courierLocalRepository.saveAccount(accountEntity))
                .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun replaceAccountsEntity(
            it: MutableList<CourierBillingAccountEntity>,
            accountEntity: CourierBillingAccountEntity
    ): MutableList<CourierBillingAccountEntity> {
        val each = it.iterator()
        while (each.hasNext()) {
            if (each.next().correspondentAccount == accountEntity.correspondentAccount)
                each.remove()
        }
        it.add(accountEntity)
        return it
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

    override fun getEditableResult(account: String): Single<EditableResult> {
        return Single.zip(
                courierLocalRepository.readAccount(account),
                courierLocalRepository.readAllAccounts().map { it.size > 1 },
                { account, isRemovable -> EditableResult(account, isRemovable) })
                .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getBank(bic: String): Maybe<BankEntity> {
        return appRemoteRepository.getBank(bic)
                .compose(rxSchedulerFactory.applyMaybeSchedulers())
    }

    override fun removeAccount(account: String): Completable {
        return courierLocalRepository.readAllAccounts()
                .map { it.toMutableList() }
                .map { removeAccount(it, account) }
                .map { it.convertToAccountEntity() }
                .flatMapCompletable { appRemoteRepository.setBankAccounts(it) }
                .andThen(courierLocalRepository.deleteAccount(account))
                .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeAccount(
            it: MutableList<CourierBillingAccountEntity>,
            account: String
    ): MutableList<CourierBillingAccountEntity> {
        val each = it.iterator()
        while (each.hasNext()) {
            if (each.next().correspondentAccount == account) each.remove()
        }
        return it
    }

}

data class EditableResult(
        val courierBillingAccountEntity: CourierBillingAccountEntity,
        val isRemovable: Boolean
)