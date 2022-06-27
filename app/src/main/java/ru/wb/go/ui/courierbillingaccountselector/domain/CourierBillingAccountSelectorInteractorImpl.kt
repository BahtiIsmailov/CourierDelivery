package ru.wb.go.ui.courierbillingaccountselector.domain

import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.exceptions.TimeoutException
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingAccountSelectorInteractorImpl(
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val appRemoteRepository: AppRemoteRepository,
    private val userManager: UserManager,
    private val tokenManager: TokenManager
) : BaseServiceInteractorImpl(networkMonitorRepository, deviceManager),
    CourierBillingAccountSelectorInteractor {

    override suspend fun payments(amount: Int, paymentEntity: PaymentEntity) {
        try {
            appRemoteRepository.payments(initGuid(), amount, paymentEntity)
        } catch (e: Exception) {
            if (e !is TimeoutException) {
                userManager.clearPaymentGuid()
                userManager.clearPaymentGuid()
            }
        }
    }

    private fun initGuid(): String {
        if (userManager.getPaymentGuid().isEmpty()) {
            userManager.savePaymentGuid(deviceManager.guid())
        }
        return userManager.getPaymentGuid()
    }

    override suspend fun getBillingAccounts(): List<CourierBillingAccountEntity> {
        val response = appRemoteRepository.getBankAccounts()
        val userName = tokenManager.userName()
        val inn = response.inn
        return response.data.convertToBillingAccounts(userName, inn)
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

