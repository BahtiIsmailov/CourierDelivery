package ru.wb.go.network.exceptions

class UnknownException(
    override val message: String,
    val extensionMessage: String
) : Exception()