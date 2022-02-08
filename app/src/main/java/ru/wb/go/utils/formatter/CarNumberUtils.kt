package ru.wb.go.utils.formatter

import ru.wb.go.ui.couriercarnumber.keyboard.CarNumberKeyboardMode

object CarNumberUtils {

    private const val MAX_PHONE_DIGITS = 11
    private const val MAX_NUMBER_DIGITS_MASK = "A 000 AA 000"

    fun numberFormat(carNumber: String): String {
        val formatNumber = StringBuilder()
        val lengthPhone = getLength(carNumber.length)
        for (position in 0 until lengthPhone) {
            if (position == 1 || position == 4 || position == 6)
                formatNumber.append(" ")
            formatNumber.append(carNumber[position])
        }
        return formatNumber.toString()
    }

    private fun getLength(lengthPhone: Int): Int {
        return if (lengthPhone > MAX_PHONE_DIGITS) MAX_PHONE_DIGITS else lengthPhone
    }

    fun numberFormatter(number: String): String {
        val numberFormat = numberFormat(number)
        return numberFormat.plus(MAX_NUMBER_DIGITS_MASK.drop(numberFormat.length))
    }

    fun numberFormatterSpanLength(number: String): Int {
        return numberFormat(number).length
    }

    @CarNumberKeyboardMode
    fun numberKeyboardMode(number: String): Int {
        val length = number.length
        return if (length == 0 || length == 4 || length == 5)
            CarNumberKeyboardMode.SYMBOL else CarNumberKeyboardMode.NUMERIC
    }
}