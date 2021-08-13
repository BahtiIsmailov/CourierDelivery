package ru.wb.perevozka.utils.formatter

import io.reactivex.Observable
import ru.wb.perevozka.R
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import kotlin.math.min

object PhoneUtils {

    private const val MAX_PHONE_FORMAT_DIGITS = R.integer.max_phone_string
    private const val MAX_PHONE_DIGITS = R.integer.max_phone_digits
    private const val MAX_PHONE_DIGITS_MASK = "+7 (000) 000-00-00"
    private const val PHONE_DIGIT_FORMAT = "[^\\d.]"

    private fun phoneFormat(phoneNumber: String): String {

        val phone = getPhoneDigits(phoneNumber)
        val formatNumber = StringBuilder()
        val lengthPhone = getLengthPhone(phone.length)
        if (lengthPhone == 0) {
            formatNumber.append("+7")
        }
        for (position in 0 until lengthPhone) {
            if (position == 0) {
                formatNumber.append("+")
            } else if (position == 1) {
                formatNumber.append(" (")
            } else if (position == 4) {
                formatNumber.append(") ")
            } else if (position == 7 || position == 9) {
                formatNumber.append("-")
            }
            formatNumber.append(phone[position])
        }
        return formatNumber.toString()
    }

    private fun getLengthPhone(lengthPhone: Int): Int {
        return if (lengthPhone > MAX_PHONE_DIGITS) MAX_PHONE_DIGITS else lengthPhone
    }

    private fun getPhoneDigits(phoneNumber: String): String {
        return phoneNumber.replace(PHONE_DIGIT_FORMAT.toRegex(), "")
    }

    fun phoneFormatter(
        observablePhone: Observable<CharSequence>,
        rxSchedulerFactory: RxSchedulerFactory,
    ): Observable<String> {
        return observablePhone
            .map { it.toString() }
            .distinctUntilChanged()
            .map { phoneFormat(it) }
            .map { it.substring(0, min(it.length, MAX_PHONE_FORMAT_DIGITS)) }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    fun phoneFormatter(number: String): String {
        val formatPhone = phoneFormat("+7".plus(number))
        return formatPhone.plus(MAX_PHONE_DIGITS_MASK.drop(formatPhone.length))
    }

    fun phoneFormatterSpanLength(number: String): Int {
        return phoneFormat("+7".plus(number)).length
    }
}