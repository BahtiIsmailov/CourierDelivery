package com.wb.logistics.utils.formatter

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.R
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import kotlin.math.min

object PhoneUtils {

    private const val TIME_OUT_DEBOUNCE = 10
    private const val MAX_PHONE_FORMAT_DIGITS = R.integer.max_phone_string
    private const val MAX_PHONE_DIGITS = R.integer.max_phone_digits
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
        observablePhone: InitialValueObservable<CharSequence>,
        rxSchedulerFactory: RxSchedulerFactory,
    ): Observable<String> {
        return observablePhone
            .map { it.toString() }
            .debounce(TIME_OUT_DEBOUNCE.toLong(), TimeUnit.MILLISECONDS)
            .map { phoneFormat(it) }
            .distinctUntilChanged()
            .map {
                it.substring(0, min(it.length, MAX_PHONE_FORMAT_DIGITS))
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }
}