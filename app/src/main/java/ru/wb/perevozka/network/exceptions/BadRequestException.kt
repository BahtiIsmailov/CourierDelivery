package ru.wb.perevozka.network.exceptions

class BadRequestException(val error: Error) : Exception()