package ru.wb.go.ui.courierdataexpects.domain

import io.reactivex.Completable
import io.reactivex.Single

interface CourierDataExpectsInteractor {

    suspend fun saveRepeatCourierDocuments()

    fun isRegisteredStatus():  String

}