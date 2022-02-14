package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.courier.StartTaskResponse

interface AppRemoteRepository {

    fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable

    fun getCourierDocuments():Single<CourierDocumentsEntity>

    fun tasksMy(orderId:Int?): Single<LocalComplexOrderEntity>

    fun reserveTask(taskID: String, carNumber: String): Completable

    fun deleteTask(taskID: String): Completable

    fun taskBoxes(taskID: String): Single<List<LocalBoxEntity>>

    fun setStartTask(taskID: String, box: LocalBoxEntity): Single<StartTaskResponse>

    fun setReadyTask(
            taskID: String,
            boxes: List<LocalBoxEntity>
    ): Single<TaskCostEntity>

    fun setIntransitTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ): Completable

    fun taskStatusesEnd(taskID: String): Completable

    fun getBillingInfo(isShowTransaction: Boolean): Single<BillingCommonEntity>

    fun payments(id: String, amount: Int, paymentEntity: PaymentEntity): Completable

    fun getBank(bic: String): Maybe<BankEntity>

    fun getBankAccounts(): Single<BankAccountsEntity>

    fun setBankAccounts(accountEntities: List<AccountEntity>): Completable

    fun appVersion(): Single<String>

}