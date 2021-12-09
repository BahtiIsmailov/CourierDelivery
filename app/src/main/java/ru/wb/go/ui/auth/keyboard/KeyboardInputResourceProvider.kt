package ru.wb.go.ui.auth.keyboard

interface KeyboardInputResourceProvider {
    fun getTimeLeftUntilRepeatText(timeLeft: String): String
    val repeatSmsButtonTitle: String
}