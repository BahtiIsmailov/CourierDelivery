package com.wb.logistics.network.exceptions

class WrongIdentityException(message: String, val serviceCode: Int) : Exception(message)