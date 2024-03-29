package ru.wb.go.ui.courierbillingaccountdata.domain

import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingAccountDataInteractorImpl(
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val appRemoteRepository: AppRemoteRepository,
) : BaseServiceInteractorImpl(networkMonitorRepository, deviceManager),
    CourierBillingAccountDataInteractor {

    override suspend fun saveBillingAccounts(accountsEntity: List<CourierBillingAccountEntity>) {
        appRemoteRepository.setBankAccounts(accountsEntity.convertToAccountEntity())
    }

    override suspend fun getBillingAccounts(): List<CourierBillingAccountEntity> {
        return appRemoteRepository.getBankAccounts().converToCourierBillingAccountsEntity()

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

    override suspend fun getBank(bic: String): BankEntity {
        return appRemoteRepository.getBank(bic)
    }


}