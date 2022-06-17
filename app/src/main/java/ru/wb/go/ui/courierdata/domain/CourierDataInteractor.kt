package ru.wb.go.ui.courierdata.domain

import io.reactivex.Observable
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierDataInteractor {
      fun observeNetworkConnected(): NetworkState
    suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity)
    suspend fun getCourierDocuments():  CourierDocumentsEntity
}