package ru.wb.go.network.token

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
}