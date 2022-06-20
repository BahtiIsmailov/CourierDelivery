package ru.wb.go.ui.courierdataexpects.domain

interface CourierDataExpectsInteractor {

    suspend fun saveRepeatCourierDocuments()

    suspend fun isRegisteredStatus():String

}