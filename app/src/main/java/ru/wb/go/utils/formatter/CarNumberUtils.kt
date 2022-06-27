package ru.wb.go.utils.formatter

import ru.wb.go.ui.couriercarnumber.keyboard.CarNumberKeyboardMode

class CarNumberUtils(number: String) {

    private val fullNumber: String
    private val fullNumberWithMask: String
    private val fullNumberSpanLength: Int

    @CarNumberKeyboardMode
    private val numberKeyboardMode: Int

    fun fullNumber() = fullNumber

    fun numberWithMask() = fullNumberWithMask.take(8)

    fun numberSpanLength() = if (fullNumberSpanLength > 8) 8 else fullNumberSpanLength

    fun regionWithMask() = fullNumberWithMask.substring(9, 12)

    fun regionSpanLength() = if (fullNumberSpanLength > 9) fullNumberSpanLength - 9 else 0

    @CarNumberKeyboardMode
    fun numberKeyboardMode() = numberKeyboardMode

    init {
        fullNumber = numberFormat(number)
        fullNumberWithMask = numberFormatWithMask(fullNumber)
        fullNumberSpanLength = fullNumber.length
        numberKeyboardMode = numberKeyboardMode(number.length)
    }

    private fun numberFormatWithMask(numberFormat: String) =
        numberFormat.plus(MAX_NUMBER_DIGITS_MASK.drop(numberFormat.length))

    private fun numberFormat(carNumber: String): String {
        val formatNumber = StringBuilder()
        val lengthPhone = getLength(carNumber.length)
        for (position in 0 until lengthPhone) {
            if (position == 1 || position == 4 || position == 6)
                formatNumber.append(" ")
            formatNumber.append(carNumber[position])
        }
        return formatNumber.toString()
    }

    private fun getLength(lengthNumber: Int): Int {
        return if (lengthNumber > MAX_NUMBER_DIGITS) MAX_NUMBER_DIGITS else lengthNumber
    }

    @CarNumberKeyboardMode
    private fun numberKeyboardMode(length: Int): Int {
        return if (length == 0 || length == 4 || length == 5)
            CarNumberKeyboardMode.SYMBOL else CarNumberKeyboardMode.NUMERIC
    }

    companion object {
        private const val MAX_NUMBER_DIGITS = 11
        private const val MAX_NUMBER_DIGITS_MASK = "A 000 AA 000"
    }

}