package ru.wb.perevozka.network.token

interface UserManager {
    fun isPhoneChanged(phone: String): Boolean
    fun savePhone(phone: String)
    fun phone(): String
    fun saveCarNumber(number: String)
    fun carNumber(): String
    fun saveStatusTask(status: String)
    fun statusTask(): String
    fun clearAll()
    fun clearStatus()
}