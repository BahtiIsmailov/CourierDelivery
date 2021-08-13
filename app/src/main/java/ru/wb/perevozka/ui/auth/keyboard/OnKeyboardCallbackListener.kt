package ru.wb.perevozka.ui.auth.keyboard

import ru.wb.perevozka.ui.auth.keyboard.KeyboardNumericView.LeftButtonMode
import ru.wb.perevozka.ui.auth.keyboard.KeyboardNumericView.RightButtonMode

interface OnKeyboardCallbackListener {
    fun onNumberClicked(number: Int)
    fun onLeftButtonClicked(mode: LeftButtonMode)
    fun onRightButtonClicked(mode: RightButtonMode)
}