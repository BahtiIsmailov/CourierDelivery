package ru.wb.perevozka.ui.auth.keyboard

interface OnKeyboardInputViewListener {
    fun onCodeEntered(pin: String)
    fun onFingerprintPressed()
    fun onForgotPasswordPressed()
    fun onRepeatSmsClicked()
}