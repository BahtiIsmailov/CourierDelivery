package ru.wb.perevozka.network.token

interface UserManager {
    fun isPhoneChanged(phone: String): Boolean
    fun savePhone(phone: String)
    fun phone(): String
    fun clear()
}