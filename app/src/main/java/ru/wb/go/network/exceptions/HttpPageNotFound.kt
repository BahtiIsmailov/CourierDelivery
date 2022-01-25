package ru.wb.go.network.exceptions

import java.lang.Exception

class HttpPageNotFound (override val message: String) : Exception()