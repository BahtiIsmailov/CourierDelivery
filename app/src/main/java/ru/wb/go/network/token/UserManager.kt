package ru.wb.go.network.token

import ru.wb.go.network.api.app.entity.CourierDocumentsEntity

interface UserManager {
    fun isPhoneChanged(phone: String): Boolean
    fun savePhone(phone: String)
    fun phone(): String
    fun saveCarNumber(number: String)
    fun carNumber(): String
    fun savePaymentGuid(number: String)
    fun clearPaymentGuid()
    fun getPaymentGuid(): String
    fun clearAll()
    fun saveCourierDocumentsEntity(courierDocumentsEntity: CourierDocumentsEntity)
    fun courierDocumentsEntity(): CourierDocumentsEntity?
    fun clearCourierDocumentsEntity()
}