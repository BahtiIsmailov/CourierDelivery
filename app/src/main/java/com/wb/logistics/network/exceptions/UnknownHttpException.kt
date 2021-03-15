package com.wb.logistics.network.exceptions

class UnknownHttpException(
    override val message: String,
    val extensionMessage: String,
    val serviceCode: Int
) : Exception()