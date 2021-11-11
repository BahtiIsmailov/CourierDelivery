package ru.wb.go.ui.auth.keyboard

interface OnKeyboardInputViewListener {
    fun onCodeEntered(pin: String)
    fun onFingerprintPressed()
    fun onForgotPasswordPressed()
    fun onRepeatSmsClicked()
}