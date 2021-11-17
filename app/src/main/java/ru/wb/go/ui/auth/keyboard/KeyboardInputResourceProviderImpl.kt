package ru.wb.go.ui.auth.keyboard

import android.content.Context
import ru.wb.go.ui.auth.keyboard.KeyboardInputResourceProvider
import ru.wb.go.R

class KeyboardInputResourceProviderImpl(private val context: Context) :
    KeyboardInputResourceProvider {
    override fun getTimeLeftUntilRepeatText(timeLeft: String): String {
        return context.getString(R.string.input_sms_repeat_status_timer, timeLeft)
    }

    override val repeatSmsButtonTitle: String
        get() = context.getString(R.string.input_sms_repeat_no_timer)
}