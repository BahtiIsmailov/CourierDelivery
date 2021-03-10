package com.wb.logistics.network.rx

interface CallAdapterFactoryResourceProvider {
    val noInternetError: String
    val timeoutServiceError: String
    val wrongIdentityError: String
    val unauthorizedError: String
}