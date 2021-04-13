package com.wb.logistics.network.token

interface UserManager {
    fun isUserChanged(phone: String): Boolean
    fun savePhone(phone: String)
    fun clear()
}