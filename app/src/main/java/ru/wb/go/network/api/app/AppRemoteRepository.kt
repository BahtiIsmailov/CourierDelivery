package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.AccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity

interface AppRemoteRepository {

    fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable
    fun getCourierDocuments():Single<CourierDocumentsEntity>

    fun courierWarehouses(): Single<List<CourierWarehouseLocalEntity>>

    fun courierOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>>

    fun tasksMy(): Single<CourierTasksMyEntity>

    fun anchorTask(taskID: String, carNumber: String): Completable

    fun deleteTask(taskID: String): Completable

    fun taskStatuses(taskID: String): Single<CourierTaskStatusesEntity>

    fun taskBoxes(taskID: String): Single<CourierTaskBoxesEntity>

    fun taskStart(taskID: String, courierTaskStartEntity: CourierTaskStartEntity): Completable

    fun taskStatusesReady(
        taskID: String,
        courierTaskStatusesIntransitEntity: List<CourierTaskStatusesIntransitEntity>
    ): Single<CourierTaskStatusesIntransitCostEntity>

    fun taskStatusesIntransit(
        taskID: String,
        courierTaskStatusesIntransitEntity: List<CourierTaskStatusesIntransitEntity>
    ): Completable

    fun taskStatusesEnd(taskID: String): Completable

    fun putCarNumbers(carNumbersEntity: List<CarNumberEntity>): Completable

    fun billing(isShowTransaction: Boolean): Single<BillingCommonEntity>

    fun payments(paymentEntity: PaymentEntity): Completable

    fun getBank(bic: String): Maybe<BankEntity>

    fun getBankAccounts(): Single<AccountsEntity>

    fun setBankAccounts(accountEntities: List<AccountEntity>): Completable

    fun appVersion(): Single<String>

}