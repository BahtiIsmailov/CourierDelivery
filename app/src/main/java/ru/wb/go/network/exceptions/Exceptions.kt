package ru.wb.go.network.exceptions

class BadRequestException(val error: Error) : Exception()

class CustomException(override val message: String) : Exception()

class ForbiddenException(override val message: String) : Exception()

class HttpObjectNotFoundException(override val message: String, val code: String) : java.lang.Exception()

class HttpPageNotFoundException (override val message: String) : Exception()

class LockedException(override val message: String) : Exception()

class InternalServerException(override val message: String) : Exception()

class NoInternetException(override val message: String) : Exception()

class TimeoutException(override val message: String) : Exception()

class UnauthorizedException(override val message: String) : Exception()

class UnknownHttpException(override val message: String) : Exception()

class UnknownException(override val message: String, val extensionMessage: String) : Exception()

