package ru.wb.go.ui

sealed class UIState<out R> {
    data class Success<out T>(val data: T) : UIState<T>()
    data class Error(val error: String) : UIState<Nothing>()
    object Loading : UIState<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$error]"
            Loading -> "Loading"
        }
    }
}