package ru.wb.go.network.exceptions

import java.lang.Exception

class HttpObjectNotFoundException(override val message: String, val code: String) : Exception()