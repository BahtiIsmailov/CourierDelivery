package ru.wb.go.network.exceptions

class UnknownHttpException(
    override val message: String,
//    val extensionMessage: String,
//    val serviceCode: Int
) : Exception()