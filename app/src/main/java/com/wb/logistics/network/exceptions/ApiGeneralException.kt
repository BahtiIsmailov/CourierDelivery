package com.wb.logistics.network.exceptions

class ApiGeneralException : Exception {
    private var code = GeneralCodeException.EMPTY_EXCEPTION
    private val logicalErrorMessage: String

    constructor(message: String, logicalErrorMessage: String) : super(message) {
        this.logicalErrorMessage = logicalErrorMessage
    }

    constructor(message: String, logicalErrorMessage: String, code: GeneralCodeException) : super(
        message
    ) {
        this.logicalErrorMessage = logicalErrorMessage
        this.code = code
    }

    fun logicalErrorMessage(): String {
        return logicalErrorMessage
    }

    fun code(): GeneralCodeException {
        return code
    }
}