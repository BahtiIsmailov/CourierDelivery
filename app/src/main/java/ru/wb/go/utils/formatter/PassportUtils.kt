package ru.wb.go.utils.formatter

object PassportUtils {

    private const val MAX_SERIES_DIGITS = 4
    private const val MAX_CODE_DIGITS = 6
    private const val MAX_DATE_DIGITS = 10
    private const val DIGIT_FORMAT = "[^\\d.]"

    private fun seriesFormat(seriesNumber: String): String {
        val series = getDigits(seriesNumber)
        val formatNumber = StringBuilder()
        val lengthSeries = getLengthSeries(series.length)
        for (position in 0 until lengthSeries) {
            if (position == 2) {
                formatNumber.append(" ")
            }
            formatNumber.append(series[position])
        }
        return formatNumber.toString()
    }

    private fun codeFormat(codeNumber: String): String {
        val code = getDigits(codeNumber)
        val formatNumber = StringBuilder()
        val lengthCode = getLengthCode(code.length)
        for (position in 0 until lengthCode) {
            if (position == 3) {
                formatNumber.append("-")
            }
            formatNumber.append(code[position])
        }
        return formatNumber.toString()
    }

    private fun getLengthSeries(lengthPhone: Int): Int {
        return if (lengthPhone > MAX_SERIES_DIGITS) MAX_SERIES_DIGITS else lengthPhone
    }

    private fun getLengthCode(lengthPhone: Int): Int {
        return if (lengthPhone > MAX_CODE_DIGITS) MAX_CODE_DIGITS else lengthPhone
    }

    private fun getDigits(seriesNumber: String): String {
        return seriesNumber.replace(DIGIT_FORMAT.toRegex(), "")
    }

    fun seriesFormatter(series: String): String {
        return seriesFormat(series)
    }

    fun codeFormatter(code: String): String {
        return codeFormat(code)
    }

}