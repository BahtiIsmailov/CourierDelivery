package com.wb.logistics.network.exceptions

class UnauthorizedException(message: String?, val serviceCode: Int, val extensionMessage: String) :
    Exception(message)