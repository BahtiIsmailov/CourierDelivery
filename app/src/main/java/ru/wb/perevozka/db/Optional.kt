package ru.wb.perevozka.db

sealed class Optional<T> {
    class Success<T>(val data: T) : Optional<T>()
    class Empty<T> : Optional<T>()
}