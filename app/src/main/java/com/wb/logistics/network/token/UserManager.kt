package com.wb.logistics.network.token

interface UserManager {
    fun isPhoneChanged(phone: String): Boolean
    fun savePhone(phone: String)
    fun phone(): String
    fun clear()
}