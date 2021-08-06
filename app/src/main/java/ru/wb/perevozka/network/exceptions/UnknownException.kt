package ru.wb.perevozka.network.exceptions

class UnknownException(
    override val message: String,
    val extensionMessage: String
) : Exception()