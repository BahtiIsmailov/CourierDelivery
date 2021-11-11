package ru.wb.go.network.exceptions

interface ErrorResolutionResourceProvider {
    val noInternetError: String
    val timeoutServiceError: String
    val wrongIdentityError: String
    val unauthorizedError: String
    val unknownError: String
    val unknownHttpError: String
}