package ru.wb.go.ui.auth.keyboard

import ru.wb.go.ui.auth.keyboard.KeyboardNumericView.LeftButtonMode
import ru.wb.go.ui.auth.keyboard.KeyboardNumericView.RightButtonMode

interface OnKeyboardCallbackListener {
    fun onNumberClicked(number: Int)
    fun onLeftButtonClicked(mode: LeftButtonMode)
    fun onRightButtonClicked(mode: RightButtonMode)
}