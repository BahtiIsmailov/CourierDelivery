package ru.wb.go.network.api.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.api.app.entity.TaskCostEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.courier.CourierAnchorResponse
import ru.wb.go.network.api.app.remote.courier.StartTaskResponse
import ru.wb.go.network.api.app.remote.courier.convertToApiBoxRequest
import ru.wb.go.network.token.TokenManager

class AppRemoteRepositoryCoroutineImpl(
    private val autentificatorIntercept: AutentificatorIntercept,
    private val remoteRepo: AppApiCoroutine,
    private val tokenManager: TokenManager
) : AppRemoteRepositoryCoroutine {

    companion object {
        const val COST_DIVIDER = 100
    }

//TODO("нужно проверить отправлется ли в метрику initNameOfMethod")

    override suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity) {
        autentificatorIntercept.initNameOfMethod("courierDocuments")
        return  withContext(Dispatchers.IO) {
            remoteRepo.saveCourierDocuments(
                tokenManager.apiVersion(),
                toCourierDocumentsDocumentsRequest(courierDocumentsEntity)
            )
        }
    }

    override suspend fun getCourierDocuments(): CourierDocumentsEntity {
        val response = withContext(Dispatchers.IO) { remoteRepo.getCourierDocuments(apiVersion()) }
        return toCourierDocumentsEntity(response)
    }

    override suspend fun tasksMy(orderId: Int?): LocalComplexOrderEntity  {
        val badOrder = initLocalOrderEntity()
        return try {
            val response = withContext(Dispatchers.IO){remoteRepo.tasksMy(apiVersion())}
            if (response.id > 0) {
                val remoteOffices = toMyTaskResponse(response)
                toLocalComplexOrderEntity(remoteOffices, response)
            } else {
                LocalComplexOrderEntity(badOrder, listOf())
            }
        }catch (e:Exception){
            LocalComplexOrderEntity(badOrder.copy(orderId = -2), listOf())
        }finally {
            autentificatorIntercept.initNameOfMethod("getMyTask")
        }
    }

    override suspend fun reserveTask(taskID: String, carNumber: String) {
        autentificatorIntercept.initNameOfMethod("reserveTask")
        return remoteRepo.reserveTask(
            apiVersion(),
            taskID,
            CourierAnchorResponse(carNumber)
        )
    }

    override suspend fun deleteTask(taskID: String) {
        autentificatorIntercept.initNameOfMethod("deleteTask")
        return remoteRepo.deleteTask(apiVersion(), taskID)
    }

    override suspend fun taskBoxes(taskID: String): List<LocalBoxEntity> {
        val response = remoteRepo.taskBoxes(apiVersion(), taskID)
        return  toListLocalBoxEntity(response)
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
    ): TaskCostEntity{
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
        return TaskCostEntity(response.cost / AppRemoteRepositoryImpl.COST_DIVIDER)

    }

    override suspend fun setIntransitTask(taskID: String, boxes: List<LocalBoxEntity>) {
        val boxesRequest = boxes.map { it.convertToApiBoxRequest() }
        autentificatorIntercept.initNameOfMethod("setIntransit")
        return remoteRepo.taskStatusesIntransit(apiVersion(), taskID, boxesRequest)
    }

    override suspend fun taskStatusesEnd(taskID: String) {
        autentificatorIntercept.initNameOfMethod("setEnd")
        return remoteRepo.taskStatusesEnd(apiVersion(), taskID)
    }

    override suspend fun getBillingInfo(isShowTransaction: Boolean): BillingCommonEntity {
         return toBillingCommonEntity(remoteRepo.getBilling(apiVersion(), isShowTransaction))
    }

    override suspend fun payments(id: String, amount: Int, paymentEntity: PaymentEntity) {
        val paymentRequest = toPaymentsRequest(id,amount,paymentEntity)
        remoteRepo.doTransaction(apiVersion(), paymentRequest)
    }

    override suspend fun getBank(bic: String): BankEntity {
        val response = remoteRepo.getBank(apiVersion(), bic)
        return with(response) {
            BankEntity(this.bic, name, correspondentAccount, isDeleted)
        }
    }

    override suspend fun getBankAccounts(): BankAccountsEntity {
        val response = remoteRepo.getBankAccounts(apiVersion())
        return with(response){
            BankAccountsEntity(this.inn, this.data.convertToEntity())
        }
    }

    override suspend fun setBankAccounts(accountEntities: List<AccountEntity>) {
        return remoteRepo.setBankAccounts(apiVersion(), accountEntities.convertToRequest())
    }

    override suspend fun appVersion(): String{
        autentificatorIntercept.initNameOfMethod("appVersion")
        return remoteRepo.getAppActualVersion(tokenManager.apiVersion()).version
    }

    private fun apiVersion() = tokenManager.apiVersion()


}