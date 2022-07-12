package ru.wb.go.network.api.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.courier.CourierAnchorResponse
import ru.wb.go.network.api.app.remote.courier.StartTaskResponse
import ru.wb.go.network.api.app.remote.courier.convertToApiBoxRequest
import ru.wb.go.network.token.TokenManager

class AppRemoteRepositoryImpl(
    private val autentificatorIntercept: AutentificatorIntercept,
    private val remoteRepo: AppApi,
    private val tokenManager: TokenManager,
) : AppRemoteRepository {

    companion object {
        const val COST_DIVIDER = 100
    }

    override suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity) {
        withContext(Dispatchers.IO) {
            remoteRepo.saveCourierDocuments(
                tokenManager.apiVersion(),
                toCourierDocumentsDocumentsRequest(courierDocumentsEntity)
            )
            autentificatorIntercept.initNameOfMethod("courierDocuments")
        }
    }

    override suspend fun getCourierDocuments(): CourierDocumentsEntity {
        return withContext(Dispatchers.IO) {
            toCourierDocumentsEntity(remoteRepo.getCourierDocuments(apiVersion()))
        }
    }

    override suspend fun tasksMy(): LocalComplexOrderEntity {
        toLocalOrderEntity()
        autentificatorIntercept.initNameOfMethod("getMyTask")
        return withContext(Dispatchers.IO) {
            try {
                val res = remoteRepo.tasksMy(apiVersion())
                if (res.id > 0) {
                    val remoteOffices = mutableListOf<LocalOfficeEntity>()
                    res.dstOffices.forEach { remoteOffices.add(toLocalOfficeEntity(it)) }
                    toLocalComplexOrderEntity(remoteOffices, res)

                } else {
                    LocalComplexOrderEntity(toLocalOrderEntity(), listOf())
                }
            } catch (e: Exception) {
//                if (orderId == null) {
//                    error(e)
//                } else
                    LocalComplexOrderEntity(toLocalOrderEntity().copy(orderId = -2), listOf())

            }
        }
    }



    override suspend fun reserveTask(
        taskID: String,
        carNumber: String
    ) {
        return withContext(Dispatchers.IO) {
            remoteRepo.reserveTask(
                apiVersion(),
                taskID,
                CourierAnchorResponse(carNumber)
            )
        }
    }

    override suspend fun deleteTask(taskID: String) {
        withContext(Dispatchers.IO) {
            autentificatorIntercept.initNameOfMethod("deleteTask")
            remoteRepo.deleteTask(apiVersion(), taskID)
        }
    }

    override suspend fun taskBoxes(taskID: String): List<LocalBoxEntity> {
        return withContext(Dispatchers.IO) {
            autentificatorIntercept.initNameOfMethod("taskBoxes")
            val response = remoteRepo.taskBoxes(apiVersion(), taskID)
            response.data.map {
                with(it) {
                    LocalBoxEntity(
                        boxId = id,
                        address = "",
                        officeId = dstOfficeID,
                        loadingAt = loadingAt,
                        deliveredAt = deliveredAt ?: ""
                    )
                }
            }
        }
    }

    override suspend fun setStartTask(
        taskID: String,
        box: LocalBoxEntity
    ): StartTaskResponse {
        return withContext(Dispatchers.IO) {
            val apiBox = box.convertToApiBoxRequest()
            val boxes = listOf(apiBox)
            autentificatorIntercept.initNameOfMethod("setStart")
            remoteRepo.setStartTask(apiVersion(), taskID, boxes)
        }
    }

    override suspend fun setReadyTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ): TaskCostEntity {
        return withContext(Dispatchers.IO) {
            val boxesRequest = boxes.map {
                assert(it.loadingAt != "")
                it.convertToApiBoxRequest()
            }
            val response = remoteRepo.taskStatusesReady(
                apiVersion(),
                taskID,
                boxesRequest
            )
            autentificatorIntercept.initNameOfMethod("setReady")
            TaskCostEntity(response.cost / COST_DIVIDER)
        }
    }

    override suspend fun setIntransitTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ) {
        withContext(Dispatchers.IO) {
            val boxesRequest = boxes.map { it.convertToApiBoxRequest() }
            autentificatorIntercept.initNameOfMethod("setIntransit")
            remoteRepo.taskStatusesIntransit(apiVersion(), taskID, boxesRequest)
        }
    }

    override suspend fun taskStatusesEnd(taskID: String) {
        withContext(Dispatchers.IO) {
            autentificatorIntercept.initNameOfMethod("setEnd")
            remoteRepo.taskStatusesEnd(apiVersion(), taskID)
        }
    }

    override suspend fun getBillingInfo(isShowTransaction: Boolean): BillingCommonEntity {
        return withContext(Dispatchers.IO) {
            val response = remoteRepo.getBilling(apiVersion(), isShowTransaction)
            val billingTransactions = mutableListOf<BillingTransactionEntity>()
            response.transactions.forEach {
                val statusOK = when (it.statusOK) {
                    null -> StatusOK.IsProcessing
                    true -> StatusOK.IsComplete
                    else -> StatusOK.IsRejected
                }
                val billing = BillingTransactionEntity(
                    statusDescription = it.statusDescription ?: "",
                    status = it.status,
                    statusOK = statusOK,
                    uuid = it.uuid,
                    value = it.value / COST_DIVIDER,
                    createdAt = it.createdAt
                )
                billingTransactions.add(billing)
            }
            BillingCommonEntity(
                id = response.id,
                balance = response.balance / COST_DIVIDER,
                entity = BillingEntity(id = response.entity.id, name = response.entity.name),
                transactions = billingTransactions
            )
        }
    }

    override suspend fun payments(id: String, amount: Int, paymentEntity: PaymentEntity) {
        withContext(Dispatchers.IO) {
            remoteRepo.doTransaction(apiVersion(), toPaymentsRequest(id, amount, paymentEntity))
        }
    }

    override suspend fun getBank(bic: String): BankEntity {
        return withContext(Dispatchers.IO) {
            val response = remoteRepo.getBank(apiDemoVersion(), bic)
            with(response) { BankEntity(response.bic, name, correspondentAccount, isDeleted) }
        }
    }

    override suspend fun getBankAccounts(): BankAccountsEntity {
        return withContext(Dispatchers.IO) {
            val response = remoteRepo.getBankAccounts(apiVersion())
            BankAccountsEntity(response.inn, response.data.convertToEntity())
        }
    }


    override suspend fun setBankAccounts(accountEntities: List<AccountEntity>) {
        return withContext(Dispatchers.IO){
            remoteRepo.setBankAccounts(apiVersion(), accountEntities.convertToRequest())
        }
    }


    override suspend fun appVersion(): String {
        return withContext(Dispatchers.IO) {
            autentificatorIntercept.initNameOfMethod("appVersion")
            remoteRepo.getAppActualVersion(apiVersion()).version
        }
    }

    private fun apiVersion() = tokenManager.apiVersion()
    private fun apiDemoVersion() = tokenManager.apiDemoVersion()

}

sealed class StatusOK {
    object IsRejected : StatusOK()
    object IsProcessing : StatusOK()
    object IsComplete : StatusOK()
}