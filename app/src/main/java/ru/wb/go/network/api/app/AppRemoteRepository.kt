package ru.wb.go.network.api.app

import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.api.app.entity.PaymentEntity
import ru.wb.go.network.api.app.entity.TaskCostEntity
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.courier.StartTaskResponse

interface AppRemoteRepository {

    suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity)

    suspend fun getCourierDocuments(): CourierDocumentsEntity

    suspend fun tasksMy(): LocalComplexOrderEntity



    suspend fun reserveTask(taskID: String, carNumber: String)

    suspend fun deleteTask(taskID: String)

    suspend fun taskBoxes(taskID: String):  List<LocalBoxEntity>

    suspend fun setStartTask(taskID: String, box: LocalBoxEntity):  StartTaskResponse

    suspend fun sendBoxOnDatabaseEveryFiveMinutes(taskID: String, box: List<LocalBoxEntity>)

    suspend fun setReadyTask(
            taskID: String,
            boxes: List<LocalBoxEntity>
    ): TaskCostEntity

    suspend fun setIntransitTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    )

     suspend fun taskStatusesEnd(taskID: String)

    suspend fun getBillingInfo(isShowTransaction: Boolean):  BillingCommonEntity

    suspend fun payments(id: String, amount: Int, paymentEntity: PaymentEntity)

    suspend fun getBank(bic: String): BankEntity

    suspend fun getBankAccounts(): BankAccountsEntity

    suspend fun setBankAccounts(accountEntities: List<AccountEntity>)

    suspend fun appVersion():  String

}