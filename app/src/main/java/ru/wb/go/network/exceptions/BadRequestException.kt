package ru.wb.go.network.exceptions

class BadRequestException(val error: Error) : Exception()