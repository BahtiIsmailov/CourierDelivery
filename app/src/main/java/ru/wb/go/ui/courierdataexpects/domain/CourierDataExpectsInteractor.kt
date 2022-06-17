package ru.wb.go.ui.courierdataexpects.domain

interface CourierDataExpectsInteractor {

    suspend fun saveRepeatCourierDocuments()

      fun isRegisteredStatus():String

}