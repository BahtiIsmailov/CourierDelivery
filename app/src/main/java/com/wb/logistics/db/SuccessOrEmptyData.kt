package com.wb.logistics.db

sealed class SuccessOrEmptyData<T> {
    class Success<T>(val data: T) : SuccessOrEmptyData<T>()
    class Empty<T> : SuccessOrEmptyData<T>()
}