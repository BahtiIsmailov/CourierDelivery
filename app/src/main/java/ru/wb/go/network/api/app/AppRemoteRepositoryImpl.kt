package ru.wb.go.network.api.app

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.entity.accounts.BankAccountsEntity
import ru.wb.go.network.api.app.entity.bank.BankEntity
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountResponse
import ru.wb.go.network.api.app.remote.courier.CourierAnchorResponse
import ru.wb.go.network.api.app.remote.courier.StartTaskResponse
import ru.wb.go.network.api.app.remote.courier.convertToApiBoxRequest
import ru.wb.go.network.api.app.remote.payments.PaymentRequest
import ru.wb.go.network.api.app.remote.payments.PaymentsRequest
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

class AppRemoteRepositoryImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val remoteRepo: AppApi,
    private val tokenManager: TokenManager,
) : AppRemoteRepository {

    companion object {
        private const val COST_DIVIDER = 100
    }

    override fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable {
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
        return remoteRepo.saveCourierDocuments(tokenManager.apiVersion(), courierDocuments)
            .compose(rxSchedulerFactory.applyCompletableMetrics("courierDocuments"))
    }

    override fun getCourierDocuments(): Single<CourierDocumentsEntity> {
        return remoteRepo.getCourierDocuments(apiVersion())
            .map { response ->
                with(response) {
                    CourierDocumentsEntity(
                        errorAnnotate = errorAnnotate,
                        firstName = firstName,
                        inn = inn,
                        middleName = middleName,
                        passportDateOfIssue = passportDateOfIssue,
                        passportDepartmentCode = passportDepartmentCode,
                        passportIssuedBy = passportIssuedBy,
                        passportNumber = passportNumber,
                        passportSeries = passportSeries,
                        surName = surName,
                    )
                }
            }
    }

    override fun tasksMy(orderId: Int?): Single<LocalComplexOrderEntity> {
        val badOrder = LocalOrderEntity(
            orderId = -1,
            routeID = 0,
            gate = "",
            minPrice = 0,
            minVolume = 0,
            minBoxes = 0,
            countOffices = 0,
            wbUserID = 0,
            carNumber = "",
            reservedAt = "",
            startedAt = "",
            reservedDuration = "",
            status = "",
            cost = 0,
            srcId = 0,
            srcName = "",
            srcAddress = "",
            srcLongitude = 0.0,
            srcLatitude = 0.0,
        )

        return remoteRepo.tasksMy(apiVersion())
            .map { task ->
                if (task.id > 0) {
                    val remoteOffices =
                        mutableListOf<LocalOfficeEntity>()
                    task.dstOffices.forEach {
                        remoteOffices.add(
                            LocalOfficeEntity(
                                officeId = it.id,
                                officeName = it.name ?: "",
                                address = it.fullAddress ?: "",
                                longitude = it.long,
                                latitude = it.lat,
                                countBoxes = 0,
                                deliveredBoxes = 0,
                                isVisited = false,
                                isOnline = false
                            )
                        )
                    }
                    LocalComplexOrderEntity(
                        order = LocalOrderEntity(
                            orderId = task.id,
                            routeID = task.routeID ?: 0,
                            gate = task.gate ?: "",
                            minPrice = task.minPrice,
                            minVolume = task.minVolume,
                            minBoxes = task.minBoxesCount,
                            countOffices = remoteOffices.size,
                            wbUserID = task.wbUserID,
                            carNumber = task.carNumber,
                            reservedAt = task.reservedAt,
                            startedAt = task.startedAt ?: "",
                            reservedDuration = task.reservedDuration,
                            status = task.status ?: "",
                            cost = (task.cost ?: 0) / COST_DIVIDER,
                            srcId = task.srcOffice.id,
                            srcName = task.srcOffice.name,
                            srcAddress = task.srcOffice.fullAddress,
                            srcLongitude = task.srcOffice.long,
                            srcLatitude = task.srcOffice.lat,
                        ),
                        offices = remoteOffices
                    )
                } else {
                    LocalComplexOrderEntity(badOrder, listOf())
                }
            }
            .onErrorResumeNext {
                if (orderId == null) {
                    Single.error(it)
                } else
                    Single.just(LocalComplexOrderEntity(badOrder.copy(orderId = -2), listOf()))
            }
            .compose(rxSchedulerFactory.applySingleMetrics("getMyTask"))

    }

    override fun reserveTask(
        taskID: String,
        carNumber: String
    ): Completable {
        return remoteRepo.reserveTask(
            apiVersion(),
            taskID,
            CourierAnchorResponse(carNumber)
        ).compose(rxSchedulerFactory.applyCompletableMetrics("reserveTask"))
    }

    override fun deleteTask(taskID: String): Completable {
        return remoteRepo.deleteTask(apiVersion(), taskID)
            .compose(rxSchedulerFactory.applyCompletableMetrics("deleteTask"))
    }

    override fun taskBoxes(taskID: String): Single<List<LocalBoxEntity>> {
        return remoteRepo.taskBoxes(apiVersion(), taskID)
            .map { response ->
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
            .compose(rxSchedulerFactory.applySingleMetrics("taskBoxes"))
    }

    override fun setStartTask(
        taskID: String,
        box: LocalBoxEntity
    ): Single<StartTaskResponse> {
        val apiBox = box.convertToApiBoxRequest()
        val boxes = listOf(apiBox)
        return remoteRepo.setStartTask(apiVersion(), taskID, boxes)
            .compose(rxSchedulerFactory.applySingleMetrics("setStart"))
    }

    override fun setReadyTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ): Single<TaskCostEntity> {

        val boxesRequest = boxes.map {
            assert(it.loadingAt != "")
            it.convertToApiBoxRequest()
        }

        return remoteRepo.taskStatusesReady(
            apiVersion(),
            taskID,
            boxesRequest
        ).map { TaskCostEntity(it.cost / COST_DIVIDER) }
            .compose(rxSchedulerFactory.applySingleMetrics("setReady"))
    }

    override fun setIntransitTask(
        taskID: String,
        boxes: List<LocalBoxEntity>
    ): Completable {
        val boxesRequest = boxes.map { it.convertToApiBoxRequest() }
        return remoteRepo.taskStatusesIntransit(apiVersion(), taskID, boxesRequest)
            .compose(rxSchedulerFactory.applyCompletableMetrics("setIntransit"))
    }

    override fun taskStatusesEnd(taskID: String): Completable {
        return remoteRepo.taskStatusesEnd(apiVersion(), taskID)
            .compose(rxSchedulerFactory.applyCompletableMetrics("setEnd"))
    }

    override fun getBillingInfo(isShowTransaction: Boolean): Single<BillingCommonEntity> {
        return remoteRepo.getBilling(apiVersion(), isShowTransaction)
            .map { response ->
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

    override fun payments(id: String, amount: Int, paymentEntity: PaymentEntity): Completable {
        val paymentRequest = with(paymentEntity) {
            PaymentsRequest(
                id = id,
                value = amount * COST_DIVIDER,
                PaymentRequest(
                    recipientBankName = recipientBankName,
                    recipientBankBik = recipientBankBik,
                    recipientCorrespondentAccount = recipientCorrespondentAccount,
                    recipientName = recipientName,
                    recipientAccount = recipientAccount,
                    recipientInn = recipientInn,
                )
            )
        }
        return remoteRepo.doTransaction(apiVersion(), paymentRequest)
    }

    override fun getBank(bic: String): Maybe<BankEntity> {
        return remoteRepo.getBank(apiVersion(), bic)
            .map {
                with(it) { BankEntity(it.bic, name, correspondentAccount, isDeleted) }
            }
    }

    override fun getBankAccounts(): Single<BankAccountsEntity> {
        return remoteRepo.getBankAccounts(apiVersion())
            .map { BankAccountsEntity(it.inn, it.data.convertToEntity()) }
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

    override fun setBankAccounts(accountEntities: List<AccountEntity>): Completable {
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

    override fun appVersion(): Single<String> {
        return remoteRepo.getAppActualVersion(tokenManager.apiVersion()).map { it.version }
            .compose(rxSchedulerFactory.applySingleMetrics("appVersion"))
    }

    private fun apiVersion() = tokenManager.apiVersion()

}

sealed class StatusOK {
    object IsRejected : StatusOK()
    object IsProcessing : StatusOK()
    object IsComplete : StatusOK()
}