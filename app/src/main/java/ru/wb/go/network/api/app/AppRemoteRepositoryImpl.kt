package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.AccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.CarNumberRequest
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountResponse
import ru.wb.go.network.api.app.remote.courier.*
import ru.wb.go.network.api.app.remote.payments.PaymentRequest
import ru.wb.go.network.api.app.remote.payments.PaymentsRequest
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.TimeManager

class AppRemoteRepositoryImpl(
        private val rxSchedulerFactory: RxSchedulerFactory,
        private val remote: AppApi,
        private val tokenManager: TokenManager,
        private val timeManager: TimeManager
) : AppRemoteRepository {

    companion object {
        private const val COST_DIVIDER = 100
    }

    override fun courierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable {
        val courierDocuments = with(courierDocumentsEntity) {
            CourierDocumentsRequest(
                    firstName = firstName,
                    surName = surName,
                    middleName = middleName,
                    inn = inn,
                    passportSeries = passportSeries,
                    passportNumber = passportNumber,
                    passportDateOfIssue = passportDateOfIssue,
                    passportIssuedBy = passportIssuedBy,
                    passportDepartmentCode = passportDepartmentCode
            )
        }
        return remote.courierDocuments(tokenManager.apiVersion(), courierDocuments)
                .compose(rxSchedulerFactory.applyCompletableMetrics("courierDocuments"))
    }

    override fun courierWarehouses(): Single<List<CourierWarehouseLocalEntity>> {
        return remote.freeTasksOffices(apiVersion())
                .map { it.data }
                .flatMap {
                    Observable.fromIterable(it)
                            .map { office -> convertCourierWarehouseEntity(office) }
                            .toList()
                }
                .compose(rxSchedulerFactory.applySingleMetrics("courierWarehouses"))
    }

    private fun convertCourierWarehouseEntity(courierOfficeResponse: CourierWarehouseResponse): CourierWarehouseLocalEntity {
        return with(courierOfficeResponse) {
            CourierWarehouseLocalEntity(
                    id = id,
                    name = name,
                    fullAddress = fullAddress,
                    longitude = long,
                    latitude = lat
            )
        }
    }

    override fun courierOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>> {
        return remote.freeTasks(apiVersion(), srcOfficeID)
                .map { it.data }
                .flatMap {
                    Observable.fromIterable(it)
                            .map { order -> convertCourierOrderEntity(order) }
                            .toList()
                }
                .compose(rxSchedulerFactory.applySingleMetrics("courierOrders"))
    }

    override fun tasksMy(): Single<CourierTasksMyEntity> {
        return remote.tasksMy(apiVersion()).map { task ->
            LogUtils { logDebugApp(task.toString()) }
            timeManager.saveStartedTaskTime(task.startedAt ?: "") //"2021-09-21T17:00:01.992+03:00"
            val courierTaskMyDstOfficesEntity = mutableListOf<CourierTaskMyDstOfficeEntity>()
            task.dstOffices.forEach {
                if (it.id != -1) {
                    val courierTaskMyDstOfficeEntity = CourierTaskMyDstOfficeEntity(
                            id = it.id,
                            name = it.name ?: "",
                            fullAddress = it.fullAddress ?: "",
                            long = it.long,
                            lat = it.lat
                    )
                    courierTaskMyDstOfficesEntity.add(courierTaskMyDstOfficeEntity)
                }
            }

            val srcOffice = with(task.srcOffice) {
                CourierTasksMySrcOfficeEntity(
                        id = id,
                        name = name,
                        fullAddress = fullAddress,
                        long = long,
                        lat = lat
                )
            }

            CourierTasksMyEntity(
                    id = task.id,
                    routeID = task.routeID ?: 0,
                    gate = task.gate ?: "",
                    srcOffice = srcOffice,
                    minPrice = task.minPrice,
                    minVolume = task.minVolume,
                    minBoxesCount = task.minBoxesCount,
                    dstOffices = courierTaskMyDstOfficesEntity,
                    wbUserID = task.wbUserID,
                    carNumber = task.carNumber,
                    reservedAt = task.reservedAt,
                    startedAt = task.startedAt ?: "",
                    reservedDuration = task.reservedDuration,
                    status = task.status ?: TaskStatus.TIMER.status,
                    cost = (task.cost ?: 0) / COST_DIVIDER
            )
        }
                .compose(rxSchedulerFactory.applySingleMetrics("tasksMy"))
    }

    override fun anchorTask(
            taskID: String,
            carNumber: String
    ): Completable {
        return remote.anchorTask(
                apiVersion(),
                taskID,
                CourierAnchorResponse(carNumber)
        ).compose(rxSchedulerFactory.applyCompletableMetrics("anchorTask"))
    }

    override fun deleteTask(taskID: String): Completable {
        return remote.deleteTask(apiVersion(), taskID)
                .compose(rxSchedulerFactory.applyCompletableMetrics("deleteTask"))
    }

    override fun taskStatuses(taskID: String): Single<CourierTaskStatusesEntity> {
        return remote.taskStatuses(apiVersion())
                .map { it.data }
                .map { courierTaskStatusesResponse ->
                    val courierTaskStatusesEntity = mutableListOf<CourierTaskStatusEntity>()
                    courierTaskStatusesResponse.forEach {
                        val courierTaskStatusEntity = CourierTaskStatusEntity(
                                status = it.status,
                                description = it.description
                        )
                        courierTaskStatusesEntity.add(courierTaskStatusEntity)
                    }
                    CourierTaskStatusesEntity(courierTaskStatusesEntity)
                }
                .compose(rxSchedulerFactory.applySingleMetrics("taskStatuses"))
    }

    override fun taskBoxes(taskID: String): Single<CourierTaskBoxesEntity> {
        return remote.taskBoxes(apiVersion(), taskID).map { response ->
            val courierTaskBoxEntity = mutableListOf<CourierTaskBoxEntity>()
            response.data.forEach {
                with(it) {
                    courierTaskBoxEntity.add(
                            CourierTaskBoxEntity(
                                    id = id,
                                    dstOfficeID = dstOfficeID,
                                    loadingAt = loadingAt,
                                    deliveredAt = deliveredAt ?: ""
                            )
                    )
                }
            }
            CourierTaskBoxesEntity(courierTaskBoxEntity, response.count)
        }.compose(rxSchedulerFactory.applySingleMetrics("taskBoxes"))
    }

    override fun taskStart(
            taskID: String,
            courierTaskStartEntity: CourierTaskStartEntity
    ): Completable {
        val courierTaskStartRequest =
                CourierTaskStartRequest(
                        id = courierTaskStartEntity.id,
                        dstOfficeID = courierTaskStartEntity.dstOfficeID,
                        loadingAt = courierTaskStartEntity.loadingAt,
                        deliveredAt = null
                )
        val boxes = listOf(courierTaskStartRequest)
//        LogUtils{logDebugApp("taskStart")}
//        return Completable.error(Throwable())
        return remote.taskStart(apiVersion(), taskID, boxes)
                .compose(rxSchedulerFactory.applyCompletableMetrics("taskStart"))
    }

    override fun taskStatusesReady(
            taskID: String,
            courierTaskStatusesIntransitEntity: List<CourierTaskStatusesIntransitEntity>
    ): Single<CourierTaskStatusesIntransitCostEntity> {
        val courierTaskStatusesIntransitRequest =
                mutableListOf<CourierTaskStatusesIntransitRequest>()
        courierTaskStatusesIntransitEntity.forEach {
            val courierTaskStatusIntransitRequest =
                    CourierTaskStatusesIntransitRequest(
                            id = it.id,
                            dstOfficeID = it.dstOfficeID,
                            loadingAt = it.loadingAt,
                            deliveredAt = it.deliveredAt
                    )
            courierTaskStatusesIntransitRequest.add(courierTaskStatusIntransitRequest)
        }
        return remote.taskStatusesReady(
                apiVersion(),
                taskID,
                courierTaskStatusesIntransitRequest
        ).map { CourierTaskStatusesIntransitCostEntity(it.cost / COST_DIVIDER) }
                .compose(rxSchedulerFactory.applySingleMetrics("taskStatusesReady"))
    }

    override fun taskStatusesIntransit(
            taskID: String,
            courierTaskStatusesIntransitEntity: List<CourierTaskStatusesIntransitEntity>
    ): Completable {
        val courierTaskStatusesIntransitRequest =
                mutableListOf<CourierTaskStatusesIntransitRequest>()
        courierTaskStatusesIntransitEntity.forEach {
            val courierTaskStatusIntransitRequest =
                    CourierTaskStatusesIntransitRequest(
                            id = it.id,
                            dstOfficeID = it.dstOfficeID,
                            loadingAt = it.loadingAt,
                            deliveredAt = it.deliveredAt
                    )
            courierTaskStatusesIntransitRequest.add(courierTaskStatusIntransitRequest)
        }

        val fromBoxCount = courierTaskStatusesIntransitRequest.size
        val unloadingBoxCount = courierTaskStatusesIntransitRequest.filter { it.deliveredAt.isNullOrEmpty() }.size
        val loadingBoxCount = courierTaskStatusesIntransitRequest.filter { it.deliveredAt != null }.size

        return remote.taskStatusesIntransit(
                apiVersion(),
                taskID,
                courierTaskStatusesIntransitRequest
        ).compose(rxSchedulerFactory.applyCompletableMetrics("taskStatusesIntransit " + "fromBoxCount " + fromBoxCount + " unloadingBoxCount " + unloadingBoxCount + " loadingBoxCount " + loadingBoxCount))
    }

    override fun taskStatusesEnd(taskID: String): Completable {
        return remote.taskStatusesEnd(apiVersion(), taskID)
                .compose(rxSchedulerFactory.applyCompletableMetrics("taskStatusesEnd"))
    }

    override fun putCarNumbers(carNumbersEntity: List<CarNumberEntity>): Completable {
        val carNumberRequest = mutableListOf<CarNumberRequest>()
        carNumbersEntity.forEach { carNumberRequest.add(CarNumberRequest(it.number, it.isDefault)) }
        return remote.putCarNumbers(apiVersion(), carNumberRequest)
                .compose(rxSchedulerFactory.applyCompletableMetrics("taskStatusesEnd"))
    }

    override fun billing(isShowTransaction: Boolean): Single<BillingCommonEntity> {
        return remote.billing(apiVersion(), isShowTransaction)
                .map {
                    val billingTransactions = mutableListOf<BillingTransactionEntity>()
                    it.transactions.forEach {
                        billingTransactions.add(
                                BillingTransactionEntity(
                                        uuid = it.uuid,
                                        value = it.value / COST_DIVIDER,
                                        createdAt = it.createdAt
                                )
                        )
                    }
                    BillingCommonEntity(
                            id = it.id,
                            balance = it.balance / COST_DIVIDER,
                            entity = BillingEntity(id = it.entity.id, name = it.entity.name),
                            transactions = billingTransactions
                    )
                }
    }

    override fun payments(id: String, amount: Int, paymentEntity: PaymentEntity): Completable {
        val paymentRequest = with(paymentEntity) {
            PaymentsRequest(
                    id = id,
                    amount = amount * COST_DIVIDER,
                    PaymentRequest(
                            recipientBankName = recipientBankName,
                            recipientBankBik = recipientBankBik,
                            recipientCorrespondentAccount = recipientCorrespondentAccount,
                            recipientName = recipientName,
                            recipientAccount = if (recipientAccount.isEmpty()) null else recipientAccount,
                            recipientInn = recipientInn,
                    )
            )
        }
        return remote.payments(apiVersion(), paymentRequest)
    }

    override fun getBank(bic: String): Maybe<BankEntity> {
        return remote.getBank(apiVersion(), bic)
                .map { with(it) { BankEntity(id, it.bic, name, correspondentAccount, isDeleted) } }
    }

    override fun getBankAccounts(): Single<AccountsEntity> {
        return remote.getBankAccounts(apiVersion())
                .map { AccountsEntity(it.inn, it.data.convertToEntity()) }
    }

    private fun List<AccountResponse>.convertToEntity(): List<AccountEntity> {
        val accountsEntity = mutableListOf<AccountEntity>()
        forEach { accountsEntity.add(with(it) { AccountEntity(bic, name, correspondentAccount) }) }
        return accountsEntity
    }

    override fun setBankAccounts(accountEntities: List<AccountEntity>): Completable {
        return remote.setBankAccounts(apiVersion(), accountEntities.convertToRequest())
    }

    private fun List<AccountEntity>.convertToRequest(): List<AccountRequest> {
        val accountsEntity = mutableListOf<AccountRequest>()
        forEach { accountsEntity.add(AccountRequest(it.bic, it.name, it.correspondentAccount)) }
        return accountsEntity
    }

    override fun appVersion(): Single<String> {
        return remote.version(tokenManager.apiVersion()).map { it.version }
                .compose(rxSchedulerFactory.applySingleMetrics("appVersion"))
    }

    private fun convertCourierOrderEntity(courierOrderResponse: CourierOrderResponse): CourierOrderEntity {
        val dstOffices = mutableListOf<CourierOrderDstOfficeEntity>()
        courierOrderResponse.dstOffices.forEach { dstOffice ->
            // TODO: 05.10.2021 убрать после исправлениня на беке получение минусового id
            if (dstOffice.id != -1) {
                dstOffices.add(
                        CourierOrderDstOfficeEntity(
                                id = dstOffice.id,
                                name = dstOffice.name ?: "",
                                fullAddress = dstOffice.fullAddress ?: "",
                                long = dstOffice.long,
                                lat = dstOffice.lat,
                        )
                )
            }
        }
        return with(courierOrderResponse) {
            CourierOrderEntity(
                    id = id,
                    routeID = routeID ?: 0,
                    gate = gate ?: "",
//                srcOffice = CourierOrderSrcOfficeEntity(
//                    id = srcOffice?.id ?: 0,
//                    name = srcOffice?.name ?: "",
//                    fullAddress = srcOffice?.fullAddress ?: "",
//                    long = srcOffice?.long ?: 0.0,
//                    lat = srcOffice?.lat ?: 0.0,
//                ),
                    minPrice = minPrice,
                    minVolume = minVolume,
                    minBoxesCount = minBoxesCount,
                    dstOffices = dstOffices,
                    reservedAt = "",
                    reservedDuration = reservedDuration
            )
        }
    }

    private fun apiVersion() = tokenManager.apiVersion()

}