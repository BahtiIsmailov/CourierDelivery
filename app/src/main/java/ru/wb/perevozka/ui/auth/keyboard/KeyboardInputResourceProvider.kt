package ru.wb.perevozka.ui.auth.keyboard

interface KeyboardInputResourceProvider {
    fun getTimeLeftUntilRepeatText(timeLeft: String): String
    val repeatSmsButtonTitle: String
}