package ru.wb.go.db

sealed class Optional<T> {
    class Success<T>(val data: T) : Optional<T>()
    class Empty<T> : Optional<T>()
}