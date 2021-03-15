package com.wb.logistics.network.exceptions

class UnknownException(
    override val message: String,
    val extensionMessage: String
) : Exception()