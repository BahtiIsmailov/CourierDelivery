package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountResponse
import ru.wb.go.network.api.app.remote.courier.CourierAnchorResponse
import ru.wb.go.network.api.app.remote.courier.StartTaskResponse
import ru.wb.go.network.api.app.remote.courier.convertToApiBoxRequest
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

class AppRemoteRepositoryImpl(
    private val autentificatorIntercept: AutentificatorIntercept,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val remoteRepo: AppApi,
    private val tokenManager: TokenManager,
) : AppRemoteRepository {

    companion object {
        const val COST_DIVIDER = 100
    }

    override suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity) {
        return withContext(Dispatchers.IO) {
            remoteRepo.saveCourierDocuments(
                tokenManager.apiVersion(),
                toCourierDocumentsDocumentsRequest(courierDocumentsEntity)
            )
            autentificatorIntercept.initNameOfMethod("courierDocuments")
        }
    }

    override suspend fun getCourierDocuments(): CourierDocumentsEntity {
        val response = withContext(Dispatchers.IO) {
            remoteRepo.getCourierDocuments(apiVersion())
        }
        return toCourierDocumentsEntity(response)
    }

    override suspend fun tasksMy(orderId: Int?): LocalComplexOrderEntity {
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
                if (orderId == null) {
                    error(e)
                } else
                    LocalComplexOrderEntity(toLocalOrderEntity().copy(orderId = -2), listOf())

            }
        }
    }

//    override suspend fun tasksMy(orderId: Int?):  LocalComplexOrderEntity  {
//        toLocalOrderEntity()
//
//        return withContext(Dispatchers.IO){
//            remoteRepo.tasksMy(apiVersion())
//                .map { task ->
//                    if (task.id > 0) {
//                        val remoteOffices =
//                            mutableListOf<LocalOfficeEntity>()
//                        task.dstOffices.forEach {
//                            remoteOffices.add(
//                                LocalOfficeEntity(
//                                    officeId = it.id,
//                                    officeName = it.name ?: "",
//                                    address = it.fullAddress ?: "",
//                                    longitude = it.long,
//                                    latitude = it.lat,
//                                    countBoxes = 0,
//                                    deliveredBoxes = 0,
//                                    isVisited = false,
//                                    isOnline = false
//                                )
//                            )
//                        }
//                        LocalComplexOrderEntity(
//                            order = LocalOrderEntity(
//                                orderId = task.id,
//                                routeID = task.routeID ?: 0,
//                                gate = task.gate ?: "",
//                                minPrice = task.minPrice,
//                                minVolume = task.minVolume,
//                                minBoxes = task.minBoxesCount,
//                                countOffices = remoteOffices.size,
//                                wbUserID = task.wbUserID,
//                                carNumber = task.carNumber,
//                                reservedAt = task.reservedAt,
//                                startedAt = task.startedAt ?: "",
//                                reservedDuration = task.reservedDuration,
//                                status = task.status ?: "",
//                                cost = (task.cost ?: 0) / COST_DIVIDER,
//                                srcId = task.srcOffice.id,
//                                srcName = task.srcOffice.name,
//                                srcAddress = task.srcOffice.fullAddress,
//                                srcLongitude = task.srcOffice.long,
//                                srcLatitude = task.srcOffice.lat,
//                                route = task.route ?: "не указан"
//                            ),
//                            offices = remoteOffices
//                        )
//                    } else {
//                        LocalComplexOrderEntity(badOrder, listOf())
//                    }
//                }
//                .onErrorResumeNext {
//                    if (orderId == null) {
//                        Single.error(it)
//                    } else
//                        Single.just(LocalComplexOrderEntity(badOrder.copy(orderId = -2), listOf()))
//                }
//                .compose(rxSchedulerFactory.applySingleMetrics("getMyTask"))
//        }
//
//    }


    override suspend fun reserveTask(
        taskID: String,
        carNumber: String
    ) {
        return remoteRepo.reserveTask(
            apiVersion(),
            taskID,
            CourierAnchorResponse(carNumber)
        )
    }

    override suspend fun deleteTask(taskID: String) {
        autentificatorIntercept.initNameOfMethod("deleteTask")
        remoteRepo.deleteTask(apiVersion(), taskID)

    }

    override suspend fun taskBoxes(taskID: String): List<LocalBoxEntity> {
        autentificatorIntercept.initNameOfMethod("taskBoxes")
        val response = remoteRepo.taskBoxes(apiVersion(), taskID)
        return response.data.map {
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

    override suspend fun setStartTask(
        taskID: String,
        box: LocalBoxEntity
    ): StartTaskResponse {
        val apiBox = box.convertToApiBoxRequest()
        val boxes = listOf(apiBox)
        autentificatorIntercept.initNameOfMethod("setStart")
        return remoteRepo.setStartTask(apiVersion(), taskID, boxes)

    }

    override suspend fun setReadyTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ): TaskCostEntity {

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
        return TaskCostEntity(response.cost / COST_DIVIDER)
    }


    override suspend fun setIntransitTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ) {
        val boxesRequest = boxes.map { it.convertToApiBoxRequest() }
        autentificatorIntercept.initNameOfMethod("setIntransit")
        return remoteRepo.taskStatusesIntransit(apiVersion(), taskID, boxesRequest)
    }

    override suspend fun taskStatusesEnd(taskID: String) {
        autentificatorIntercept.initNameOfMethod("setEnd")
        return remoteRepo.taskStatusesEnd(apiVersion(), taskID)
    }

    override suspend fun getBillingInfo(isShowTransaction: Boolean): BillingCommonEntity {
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
        return BillingCommonEntity(
            id = response.id,
            balance = response.balance / COST_DIVIDER,
            entity = BillingEntity(id = response.entity.id, name = response.entity.name),
            transactions = billingTransactions
        )
    }

    override suspend fun payments(id: String, amount: Int, paymentEntity: PaymentEntity) {

        return remoteRepo.doTransaction(apiVersion(), toPaymentsRequest(id, amount, paymentEntity))
    }

    override suspend fun getBank(bic: String): BankEntity {
        val response = remoteRepo.getBank(apiVersion(), bic)
        return with(response) { BankEntity(response.bic, name, correspondentAccount, isDeleted) }
    }

    override suspend fun getBankAccounts():  BankAccountsEntity  {
        val response = remoteRepo.getBankAccounts(apiVersion())
        return    BankAccountsEntity(response.inn, response.data.convertToEntity())
    }

    private fun List<AccountResponse>.convertToEntity(): List<AccountEntity> {
        val accountsEntity = mutableListOf<AccountEntity>()
        forEach {
            accountsEntity.add(with(it) {
                AccountEntity(
                    bic,
                    name,
                    correspondentAccount,
                    account
                )
            })
        }
        return accountsEntity
    }

    override suspend fun setBankAccounts(accountEntities: List<AccountEntity>){
        return remoteRepo.setBankAccounts(apiVersion(), accountEntities.convertToRequest())
    }

    private fun List<AccountEntity>.convertToRequest(): List<AccountRequest> {
        val accountsEntity = mutableListOf<AccountRequest>()
        forEach {
            accountsEntity.add(
                AccountRequest(
                    it.bic,
                    it.name,
                    it.correspondentAccount,
                    it.account
                )
            )
        }
        return accountsEntity
    }

    override suspend fun appVersion(): String  {
        autentificatorIntercept.initNameOfMethod("appVersion")
        return withContext(Dispatchers.IO){
            remoteRepo.getAppActualVersion(tokenManager.apiVersion()).version
        }

    }

    private fun apiVersion() = tokenManager.apiVersion()

}

sealed class StatusOK {
    object IsRejected : StatusOK()
    object IsProcessing : StatusOK()
    object IsComplete : StatusOK()
}