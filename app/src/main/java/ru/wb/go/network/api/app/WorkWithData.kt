package ru.wb.go.network.api.app

import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.entity.*
import ru.wb.go.network.api.app.entity.accounts.AccountEntity
import ru.wb.go.network.api.app.remote.CourierDocumentsRequest
import ru.wb.go.network.api.app.remote.accounts.AccountRequest
import ru.wb.go.network.api.app.remote.accounts.AccountResponse
import ru.wb.go.network.api.app.remote.billing.BillingCommonResponse
import ru.wb.go.network.api.app.remote.courier.*
import ru.wb.go.network.api.app.remote.payments.PaymentRequest
import ru.wb.go.network.api.app.remote.payments.PaymentsRequest


fun toCourierDocumentsDocumentsRequest(courierDocumentsEntity: CourierDocumentsEntity): CourierDocumentsRequest {
    return with(courierDocumentsEntity) {
        CourierDocumentsRequest(
            firstName = firstName,
            surName = surName,
            middleName = middleName,
            inn = inn,
            passportSeries = passportSeries,
            passportNumber = passportNumber,
            passportDateOfIssue = passportDateOfIssue,
            passportIssuedBy = passportIssuedBy,
            passportDepartmentCode = passportDepartmentCode,
            courierType = courierType
        )
    }
}



fun toCourierDocumentsEntity(courierDocumentsResponse: CourierDocumentsResponse): CourierDocumentsEntity {
    return with(courierDocumentsResponse) {
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
            courierType = courierType
        )
    }
}

fun initLocalOrderEntity(): LocalOrderEntity {
    return LocalOrderEntity(
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
        route = ""
    )
}

fun toMyTaskResponse(myTaskResponse: MyTaskResponse): MutableList<LocalOfficeEntity> {
    val remoteOffices = mutableListOf<LocalOfficeEntity>()
    myTaskResponse.dstOffices.forEach {
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

    return remoteOffices
}


fun toLocalComplexOrderEntity(
    remoteOffices: MutableList<LocalOfficeEntity>,
    myTaskResponse: MyTaskResponse
): LocalComplexOrderEntity {
    return LocalComplexOrderEntity(
        order = LocalOrderEntity(
            orderId = myTaskResponse.id,
            routeID = myTaskResponse.routeID ?: 0,
            gate = myTaskResponse.gate ?: "",
            minPrice = myTaskResponse.minPrice,
            minVolume = myTaskResponse.minVolume,
            minBoxes = myTaskResponse.minBoxesCount,
            countOffices = remoteOffices.size,
            wbUserID = myTaskResponse.wbUserID,
            carNumber = myTaskResponse.carNumber,
            reservedAt = myTaskResponse.reservedAt,
            startedAt = myTaskResponse.startedAt ?: "",
            reservedDuration = myTaskResponse.reservedDuration,
            status = myTaskResponse.status ?: "",
            cost = (myTaskResponse.cost ?: 0) / AppRemoteRepositoryImpl.COST_DIVIDER,
            srcId = myTaskResponse.srcOffice.id,
            srcName = myTaskResponse.srcOffice.name,
            srcAddress = myTaskResponse.srcOffice.fullAddress,
            srcLongitude = myTaskResponse.srcOffice.long,
            srcLatitude = myTaskResponse.srcOffice.lat,
            route = myTaskResponse.route ?: "не указан"
        ),
        offices = remoteOffices
    )
}


fun toListLocalBoxEntity(courierTaskBoxesResponse: CourierTaskBoxesResponse): List<LocalBoxEntity> {
    return courierTaskBoxesResponse.data.map {
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


fun toBillingCommonEntity(billingCommonResponse: BillingCommonResponse): BillingCommonEntity {

    val billingTransactions = mutableListOf<BillingTransactionEntity>()

    billingCommonResponse.transactions.forEach {
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
            value = it.value / AppRemoteRepositoryImpl.COST_DIVIDER,
            createdAt = it.createdAt
        )
        billingTransactions.add(billing)
    }
    return BillingCommonEntity(
        id = billingCommonResponse.id,
        balance = billingCommonResponse.balance / AppRemoteRepositoryImpl.COST_DIVIDER,
        entity = BillingEntity(
            id = billingCommonResponse.entity.id,
            name = billingCommonResponse.entity.name
        ),
        transactions = billingTransactions
    )

}

fun toPaymentsRequest(id: String, amount: Int, paymentEntity: PaymentEntity) : PaymentsRequest{
    return with(paymentEntity) {
        PaymentsRequest(
            id = id,
            value = amount * AppRemoteRepositoryImpl.COST_DIVIDER,
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
}

fun toCourierOrderDstOfficeEntity(courierOrderDstOfficeResponse: CourierOrderDstOfficeResponse): CourierOrderDstOfficeEntity{
    return CourierOrderDstOfficeEntity(
        id = courierOrderDstOfficeResponse.id,
        name = courierOrderDstOfficeResponse.name ?: "",
        fullAddress = courierOrderDstOfficeResponse.fullAddress ?: "",
        long = courierOrderDstOfficeResponse.long,
        lat = courierOrderDstOfficeResponse.lat,
        workTimes = courierOrderDstOfficeResponse.wrkTime ?: "",
        isUnusualTime = courierOrderDstOfficeResponse.unusualTime
    )
}

fun toCourierOrderEntity(courierOrderResponse: CourierOrderResponse,dstOffices:MutableList<CourierOrderDstOfficeEntity>): CourierOrderEntity{
    return CourierOrderEntity (
        id = courierOrderResponse.id,
        routeID = courierOrderResponse.routeID ?: 0,
        gate = courierOrderResponse.gate ?: "",
        minPrice = courierOrderResponse.minPrice,
        minVolume = courierOrderResponse.minVolume,
        minBoxesCount = courierOrderResponse.minBoxesCount,
        dstOffices =  dstOffices,
        reservedAt = "",
        reservedDuration = courierOrderResponse.reservedDuration,
        route = courierOrderResponse.route ?: "не указан",
        taskDistance = courierOrderResponse.taskDistance
    )
}

fun toLocalOrderEntity():LocalOrderEntity {
    return LocalOrderEntity(
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
        route = ""
    )
}

fun toLocalOfficeEntity(myDstOfficeResponse: MyDstOfficeResponse):LocalOfficeEntity{
    return LocalOfficeEntity(
        officeId = myDstOfficeResponse.id,
        officeName = myDstOfficeResponse.name ?: "",
        address = myDstOfficeResponse.fullAddress ?: "",
        longitude = myDstOfficeResponse.long,
        latitude = myDstOfficeResponse.lat,
        countBoxes = 0,
        deliveredBoxes = 0,
        isVisited = false,
        isOnline = false
    )
}
fun convertCourierWarehouseEntity(courierOfficeResponse: CourierWarehouseResponse): CourierWarehouseLocalEntity {
    return with(courierOfficeResponse) {
        CourierWarehouseLocalEntity(
            id = id,
            name = name,
            fullAddress = fullAddress,
            longitude = long,
            latitude = lat,
            distanceFromUser = 0.0F
        )
    }
}
fun convertCourierOrderEntity(courierOrderResponse: CourierOrderResponse): CourierOrderEntity {
    val dstOffices = mutableListOf<CourierOrderDstOfficeEntity>()
    courierOrderResponse.dstOffices.forEach { dstOffice ->
        if (dstOffice.id != -1) {
            dstOffices.add(toCourierOrderDstOfficeEntity(dstOffice))
        }//широта долгота +
    }
    return toCourierOrderEntity(courierOrderResponse, dstOffices)

}


fun List<AccountResponse>.convertToEntity(): List<AccountEntity> {
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

fun List<AccountEntity>.convertToRequest(): List<AccountRequest> {
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